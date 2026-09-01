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

package controllers.upload

import controllers.actions._
import models.fileSubmission.FileStatus.Failed
import pages.ExtractedFileDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StubService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{FileCheckResultHelper, LoggerUtil}
import views.html.upload.FileFailedChecksView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FileFailedChecksController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    stubService: StubService,
    fileCheckResultHelper: FileCheckResultHelper,
    val controllerComponents: MessagesControllerComponents,
    view: FileFailedChecksView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData() andThen requireData).async { implicit request =>
      stubService.getFileStatus(request.carfId).value.map {
        case Right(Failed) =>
          request.userAnswers.get(ExtractedFileDetailsPage).map(_.messageRefId) match {
            case Some(value) =>
              val summaryList =
                fileCheckResultHelper.summaryList(
                  messageRefId = value,
                  fileStatus = Failed,
                  messagePrefix = "fileFailedChecks"
                )

              Ok(view(summaryList))

            case None =>
              LoggerUtil
                .logWarn("[FileFailedChecksController][onPageLoad] ExtractedFileDetailsPage missing from UserAnswers")
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }

        case Right(otherStatus) =>
          LoggerUtil.logWarn(s"[FileFailedChecksController][onPageLoad] File status was: $otherStatus")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

        case Left(error) =>
          LoggerUtil.logWarn(s"[FileFailedChecksController][onPageLoad] Error retrieving file status: $error")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
}
