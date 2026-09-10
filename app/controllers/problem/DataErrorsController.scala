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

package controllers.problem

import config.{Constants, FrontendAppConfig}
import controllers.actions.*
import models.problem.SchemaError
import pages.{UploadSuccessDetailsPage, XmlErrorsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.*
import views.html.problem.DataErrorsView

import javax.inject.Inject

class DataErrorsController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    uploadCompletionLock: UploadCompletionLockAction,
    appConfig: FrontendAppConfig,
    val controllerComponents: MessagesControllerComponents,
    view: DataErrorsView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData() andThen uploadCompletionLock andThen requireData) { implicit request =>
      val userAnswers = request.userAnswers

      (userAnswers.get(XmlErrorsPage), userAnswers.get(UploadSuccessDetailsPage).map(_.fileName)) match {
        case (Some(xmlErrors), Some(fileName)) if xmlErrors.nonEmpty =>
          val hasMoreThanMax = xmlErrors.length > Constants.maxErrorsShown
          // TODO: Map XML errors to required content and HTML (CARF-591)
          val schemaErrors   = xmlErrors.map { error =>
            SchemaError(error.lineNumber, Text(error.errorMessage).asHtml)
          }
          Ok(view(fileName, schemaErrors.take(Constants.maxErrorsShown), hasMoreThanMax, appConfig.managementUrl))

        case (xmlErrors, _) =>
          logWarn(
            s"Unable to retrieve data errors or file name for data-errors page. Errors length: ${xmlErrors.map(_.length)}"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
}
