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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.responses.{getEmails, getName}
import pages.RcaspDetailsPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{DateTimeFormats, FileConfirmationHelper}
import views.html.FileConfirmationView
import viewmodels.govuk.all.SummaryListViewModel
import viewmodels.govuk.summarylist.FluentSummaryList

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject

class FileConfirmationController @Inject (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    view: FileConfirmationView,
    config: FrontendAppConfig,
    helper: FileConfirmationHelper,
    clock: Clock,
    val controllerComponents: MessagesControllerComponents
) extends FrontendBaseController
    with I18nSupport {

  lazy private val recovery = Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  def onPageLoad(): Action[AnyContent] = (identify() andThen getData() andThen requireData) { implicit request =>
    val userAnswers = request.userAnswers

    userAnswers.get(RcaspDetailsPage).fold(recovery) { rcaspDetails =>
      helper
        .rows(userAnswers, rcaspDetails.getName)
        .fold(recovery) { summaryListRows =>
          val summary = SummaryListViewModel(rows = summaryListRows).withCssClass("govuk-!-margin-bottom-5")

          val formattedDate    = DateTimeFormats.dateTimeToString(LocalDateTime.now(clock))
          val isRcaspUser      = rcaspDetails.IsRCASPUser
          val emailAddressHtml = generateEmailAddressHtml(rcaspDetails.getEmails, isRcaspUser)

          Ok(view(summary, formattedDate, isRcaspUser, config.managementUrl, emailAddressHtml))
        }
    }
  }

  private def generateEmailAddressHtml(emailAddresses: List[String], isRCASPUser: Boolean)(implicit
      messages: Messages
  ): String =
    emailAddresses match {
      case primary :: secondary :: Nil if isRCASPUser =>
        messages("fileConfirmation.2.email.sent", primary, secondary)
      case primary :: Nil                             =>
        messages("fileConfirmation.1.email.sent", primary)
      case Nil                                        => ""
      case _                                          =>
        val emailToApplyComma = emailAddresses.take(emailAddresses.size - 1)
        val lastEmail         = emailAddresses.last
        messages("fileConfirmation.2.email.sent", emailToApplyComma.mkString(", "), lastEmail)
    }
}
