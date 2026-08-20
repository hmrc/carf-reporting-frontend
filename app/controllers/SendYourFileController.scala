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
import models.ReportType
import models.responses.getName
import pages.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.logWarn
import views.html.SendYourFileView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SendYourFileController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    view: SendYourFileView,
    appConfig: FrontendAppConfig,
    val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify() andThen getData() andThen requireData) { implicit request =>
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

  def onSubmit(): Action[AnyContent] = (identify() andThen getData() andThen requireData) { implicit request =>
    // TODO: Build request for FTS (details TBC) and call submission connector (CARF-611)
    request.userAnswers
      .get(ExtractedFileDetailsPage)
      .fold {
        logWarn("[SendYourFileController][onSubmit] Unable to get ExtractedFileDetails from user answers")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      } { extractedFileDetails =>
        Redirect(controllers.routes.StillCheckingYourFileController.onPageLoad().url)
      }
  }

  def getFileStatusAndRedirect(): Action[AnyContent] = (identify() andThen getData() andThen requireData) {
    implicit request =>
      // TODO: Call backend to check file status (CARF-621)
      Redirect(
        controllers.routes.PlaceholderController.onPageLoad("Redirect to next page based on file status (CARF-621)").url
      )
  }
}
