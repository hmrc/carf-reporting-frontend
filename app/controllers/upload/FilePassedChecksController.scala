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
import models.fileSubmission.FileStatus.Passed
import pages.ExtractedFileDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StubService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.*
import views.html.upload.FilePassedChecksView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FilePassedChecksController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    stubService: StubService,
    val controllerComponents: MessagesControllerComponents,
    view: FilePassedChecksView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify() andThen getData()).async { implicit request =>
    val carfId       = request.carfId
    val messageRefId = request.userAnswers.flatMap(_.get(ExtractedFileDetailsPage)).map(_.messageRefId)

    stubService.getFileStatus(carfId).value.map {
      case Right(Passed) =>
        messageRefId match {
          case Some(refId) => Ok(view(refId))
          case None        =>
            logWarn("Unable to display file-passed-checks page. ExtractedFileDetailsPage missing from UserAnswers.")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }

      case Right(otherStatus) =>
        logWarn(s"Unable to display file-passed-checks page. Status was: $otherStatus")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

      case Left(error) =>
        logWarn(s"Unable to display file-passed-checks page. Error retrieving status: $error")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
