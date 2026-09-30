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

package controllers.submission

import config.FrontendAppConfig
import connectors.SubmissionDetailsConnector
import controllers.actions.*
import models.fileSubmission.SubmissionDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.logWarn
import views.html.submission.ResultOfAutomaticChecksView

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ResultOfAutomaticChecksController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    appConfig: FrontendAppConfig,
    submissionDetailsConnector: SubmissionDetailsConnector,
    val controllerComponents: MessagesControllerComponents,
    view: ResultOfAutomaticChecksView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>
    val carfId = request.carfId

    submissionDetailsConnector.getSubmissionDetailsByCarfId(carfId).value.map {
      case Right(submissions) if submissions.nonEmpty =>
        Ok(view(sortedByMostRecent(submissions), appConfig.managementUrl))
      case Right(_)                                   =>
        logWarn(
          "[ResultOfAutomaticChecksController][onPageLoad] No submissions found for result-of-automatic-checks page."
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Left(error)                                =>
        logWarn(
          s"[ResultOfAutomaticChecksController][onPageLoad] Unable to retrieve submissions for result-of-automatic-checks page. Error: $error"
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  private def sortedByMostRecent(submissions: Seq[SubmissionDetails]): Seq[SubmissionDetails] =
    submissions.sortBy(_.submissionTime)(Ordering[Instant].reverse)
}
