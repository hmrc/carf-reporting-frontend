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

import controllers.actions.IdentifierAction
import models.filecheck.FileCheckStatus.Passed
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StubService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.*
import views.html.upload.FilePassedChecksView

import javax.inject.Inject

class FilePassedChecksController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    stubService: StubService,
    val controllerComponents: MessagesControllerComponents,
    view: FilePassedChecksView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify() { implicit request =>
    stubService.getFileCheckResult(request.carfId) match {
      case Some(result) if result.status == Passed =>
        Ok(view(result.messageRefId))

      case result =>
        logWarn(
          s"Unable to display file-passed-checks page. File-check result present: ${result.isDefined}"
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
