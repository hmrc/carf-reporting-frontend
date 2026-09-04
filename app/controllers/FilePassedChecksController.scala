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
import controllers.actions.*
import models.fileSubmission.FileStatus.Passed
import pages.{ExtractedFileDetailsPage, UploadIdPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.XmlFileDetailsStubService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.logWarn
import utils.FileCheckResultHelper
import views.html.upload.FilePassedChecksView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FilePassedChecksController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    uploadCompletionLock: UploadCompletionLockAction,
    stubService: XmlFileDetailsStubService,
    fileCheckResultHelper: FileCheckResultHelper,
    val controllerComponents: MessagesControllerComponents,
    view: FilePassedChecksView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData() andThen uploadCompletionLock andThen requireData).async { implicit request =>
      (request.userAnswers.get(ExtractedFileDetailsPage), request.userAnswers.get(UploadIdPage))
        .mapN { (extractedFileDetails, uploadId) =>
          // TODO: Replace StubService method with actual call to check file status (CARF-621)
          stubService.getFileStatus(request.carfId).value.map {
            case Right(Passed) =>
              val summaryList =
                fileCheckResultHelper.summaryList(
                  messageRefId = extractedFileDetails.messageRefId,
                  fileStatus = Passed,
                  messagePrefix = "filePassedChecks"
                )
              Ok(view(summaryList, uploadId.value))

            case Right(otherStatus) =>
              logWarn(s"[FilePassedChecksController][onPageLoad] File status was: $otherStatus")
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

            case Left(error) =>
              logWarn(s"[FilePassedChecksController][onPageLoad] Error retrieving file status: $error")
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
        }
        .getOrElse {
          logWarn(
            "[FileFailedChecksController][onPageLoad] ExtractedFileDetailsPage or UploadId missing from UserAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }
}
