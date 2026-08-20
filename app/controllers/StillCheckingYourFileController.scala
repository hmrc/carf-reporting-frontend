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
import controllers.actions.*
import models.fileSubmission.FileStatus
import models.responses.getName
import pages.{ExtractedFileDetailsPage, RcaspDetailsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StubService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.logWarn
import utils.StillCheckingYourFileHelper
import views.html.StillCheckingYourFileView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StillCheckingYourFileController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    view: StillCheckingYourFileView,
    stillCheckingYourFileHelper: StillCheckingYourFileHelper,
    appConfig: FrontendAppConfig,
    stubService: StubService,
    val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify() andThen getData() andThen requireData).async { implicit request =>
    val userAnswers = request.userAnswers

    (userAnswers.get(RcaspDetailsPage), userAnswers.get(ExtractedFileDetailsPage))
      .mapN { (rcaspDetails, extractedFileDetails) =>
        // TODO: Replace StubService method with actual call to check file status (CARF-621)
        stubService.getFileStatus(request.carfId, userAnswers).value.map {
          case Right(fileStatus) =>
            fileStatus match {
              case FileStatus.Pending                =>
                val summaryList =
                  stillCheckingYourFileHelper.stillCheckingYourFileSummaryList(extractedFileDetails.messageRefId)
                Ok(
                  view(
                    summaryList,
                    appConfig.managementUrl,
                    rcaspDetails.IsRCASPUser,
                    rcaspDetails.getName
                  )
                )
              case FileStatus.Passed                 =>
                Redirect(
                  controllers.routes.PlaceholderController
                    .onPageLoad("Should redirect to /file-passed-checks (CARF-617)")
                    .url
                )
              case FileStatus.Failed                 =>
                Redirect(
                  controllers.routes.PlaceholderController
                    .onPageLoad("Should redirect to /file-failed-checks (CARF-617)")
                    .url
                )
              case FileStatus.VirusFound             =>
                Redirect(
                  controllers.routes.PlaceholderController
                    .onPageLoad("Should redirect to /problem/virus-found (CARF-617)")
                    .url
                )
              case FileStatus.UnprocessableErrorFile =>
                Redirect(
                  controllers.routes.PlaceholderController
                    .onPageLoad("Should redirect to /problem/file-not-accepted (ticket TBC)")
                    .url
                )
              case FileStatus.UnexpectedError        =>
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)
            }
          case Left(error)       =>
            logWarn(s"[StillCheckingYourFileController][onPageLoad] Error getting file status: $error")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
      .getOrElse {
        logWarn(
          "[StillCheckingYourFileController][onPageLoad] Unable to get RCASP details or ExtractedFileDetails from user answers"
        )
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))
      }
  }
}
