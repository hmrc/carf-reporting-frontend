/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import cats.syntax.all.*
import config.FrontendAppConfig
import connectors.SDESConnector
import connectors.SubmissionDetailsConnector
import controllers.actions.*
import models.ReportType
import models.fileSubmission.{FileStatus, URL}
import models.requests.sdes.{FileName, SubmissionRequest}
import models.responses.getName
import pages.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.logWarn
import views.html.SendYourFileView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SendYourFileController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    uploadCompletionLock: UploadCompletionLockAction,
    view: SendYourFileView,
    submissionDetailsConnector: SubmissionDetailsConnector,
    appConfig: FrontendAppConfig,
    val controllerComponents: MessagesControllerComponents,
    sdesConnector: SDESConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData() andThen uploadCompletionLock andThen requireData) { implicit request =>
      val userAnswers = request.userAnswers

      (userAnswers.get(RcaspDetailsPage), userAnswers.get(ExtractedFileDetailsPage))
        .mapN { (rcaspDetails, extractedFileDetails) =>
          val maybeWarningMessage =
            ReportType.warningMessageForReportType(extractedFileDetails.getReportType, rcaspDetails.getName)
          Ok(view(maybeWarningMessage, appConfig.spinnerMaxPollingAttempts))
        }
        .getOrElse {
          logWarn(
            "[SendYourFileController][onPageLoad] Unable to get RCASP details or ExtractedFileDetails from user answers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
    }

  def onSubmit(): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>
    (
      request.userAnswers.get(ExtractedFileDetailsPage),
      request.userAnswers.get(SubscriptionDetailsPage),
      request.userAnswers.get(RcaspDetailsPage),
      request.userAnswers.get(UploadDetailsUserAnswers),
      request.userAnswers.get(UploadIdPage)
    ).mapN { (extractedFileDetails, subscriptionDetails, rcaspDetails, uploadDetails, uploadId) =>
      sdesConnector
        .sendSubmission(
          SubmissionRequest(
            FileName(uploadDetails.name),
            uploadId,
            uploadDetails.size,
            uploadDetails.downloadUrl,
            checksum = uploadDetails.checksum,
            rcaspDetails,
            subscriptionDetails,
            extractedFileDetails
          )
        )
        .map(_ => extractedFileDetails)
    }.fold {
      logWarn("[SendYourFileController][onSubmit] Unable to get needed data from user answers")
      Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))
    } {
      _.value.map {
        case Right(extractedFileDetails) =>
          Redirect(controllers.routes.StillCheckingYourFileController.onPageLoad())
        case Left(error)                 => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }

  def getFileStatusAndRedirect: Action[AnyContent] =
    (identify andThen getData() andThen requireData).async { implicit request =>
      request.userAnswers
        .get(UploadIdPage)
        .fold {
          logWarn(
            "[SendYourFileController][getFileStatusAndRedirect] UploadId not found in user answers"
          )
          Future.successful(InternalServerError)
        } { uploadId =>
          submissionDetailsConnector.getFileStatus(uploadId).value.map {
            case Right(fileStatus) =>
              fileStatus match {
                case FileStatus.Pending                =>
                  NoContent
                case FileStatus.Passed                 =>
                  Ok(Json.toJson(URL(controllers.routes.FileConfirmationController.onPageLoad(uploadId.value).url)))
                case FileStatus.Failed                 =>
                  Ok(Json.toJson(URL(controllers.problem.routes.RulesErrorsController.onPageLoad(uploadId.value).url)))
                case FileStatus.VirusFound             =>
                  Ok(Json.toJson(URL(controllers.problem.routes.VirusFoundController.onPageLoad(uploadId.value).url)))
                case FileStatus.UnprocessableErrorFile =>
                  Ok(
                    Json.toJson(
                      URL(
                        controllers.routes.PlaceholderController
                          .onPageLoad("Should redirect to /problem/file-not-accepted (ticket TBC)")
                          .url
                      )
                    )
                  )
                case FileStatus.UnexpectedError        =>
                  Ok(Json.toJson(URL(controllers.routes.JourneyRecoveryController.onPageLoad().url)))
              }
            case Left(error)       =>
              logWarn(s"[SendYourFileController][getFileStatusAndRedirect] Error getting file status: $error")
              InternalServerError
          }
        }
    }
}
