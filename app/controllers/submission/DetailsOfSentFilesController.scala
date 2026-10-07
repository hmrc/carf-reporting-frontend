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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.Pagination
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.*
import viewmodels.govuk.pagination.PaginationViewModel
import views.html.submission.DetailsOfSentFilesView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DetailsOfSentFilesController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    appConfig: FrontendAppConfig,
    submissionDetailsConnector: SubmissionDetailsConnector,
    val controllerComponents: MessagesControllerComponents,
    view: DetailsOfSentFilesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(page: Int): Action[AnyContent] = identify.async { implicit request =>
    submissionDetailsConnector.getSubmissionDetailsByCarfId(request.carfId, page).value.map {
      case Right(detailsOfFilesSent) if detailsOfFilesSent.submissionRecords.nonEmpty =>
        val maybePagination: Option[Pagination] =
          if (detailsOfFilesSent.totalPages > 1) {
            Some(
              PaginationViewModel(
                currentPage = page,
                totalPages = detailsOfFilesSent.totalPages,
                call = page => controllers.submission.routes.DetailsOfSentFilesController.onPageLoad(page)
              )
            )
          } else { None }
        Ok(view(detailsOfFilesSent.submissionRecords, maybePagination, appConfig.managementUrl))
      case Right(detailsOfFilesSent)
          if detailsOfFilesSent.submissionRecords.isEmpty && detailsOfFilesSent.totalPages > 0 =>
        logInfo(s"[DetailsOfSentFilesController][onPageLoad] Page $page does not exist, redirecting to first page")
        Redirect(controllers.submission.routes.DetailsOfSentFilesController.onPageLoad())
      case Right(_)                                                                   =>
        logWarn("[DetailsOfSentFilesController][onPageLoad] No submissions found for details-of-sent-files page.")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Left(error)                                                                =>
        logWarn(
          s"[DetailsOfSentFilesController][onPageLoad] Unable to retrieve submissions for details-of-sent-files page. Error: $error"
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
