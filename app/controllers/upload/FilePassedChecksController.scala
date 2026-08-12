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
import javax.inject.Inject
import models.filecheck.FileCheckStatus.Passed
import pages.ExtractedFileDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FileCheckStatusStubService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil._
import views.html.upload.FilePassedChecksView

class FilePassedChecksController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    fileCheckStatusStubService: FileCheckStatusStubService,
    val controllerComponents: MessagesControllerComponents,
    view: FilePassedChecksView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify() andThen getData()) { implicit request =>
    val carfId       = request.carfId
    val messageRefId = request.userAnswers.flatMap(_.get(ExtractedFileDetailsPage)).map(_.messageRefId)
    val checkStatus  = fileCheckStatusStubService.getFileCheckStatus(carfId)

    (checkStatus, messageRefId) match {
      case (Some(Passed), Some(refId)) =>
        Ok(view(refId))

      case (status, refId) =>
        logWarn(
          s"Unable to display file-passed-checks page. Status present: ${status.isDefined}, MessageRefId present: ${refId.isDefined}"
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
