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

import config.FrontendAppConfig
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.fileSubmission.FileStatus.Passed
import models.responses.{getEmails, getName}
import models.{CachedFileDetails, ExtractedFileDetails}
import pages.UploadCompletionLockPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.XmlFileDetailsStubService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.logWarn
import utils.{DateTimeFormats, FileConfirmationHelper}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.FileConfirmationView

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileConfirmationController @Inject (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    stubService: XmlFileDetailsStubService,
    sessionRepository: SessionRepository,
    view: FileConfirmationView,
    config: FrontendAppConfig,
    helper: FileConfirmationHelper,
    val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def recovery(message: String) = {
    logWarn(s"[FileConfirmationController][onPageLoad] $message")
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
  }

  def onPageLoad(uploadId: String): Action[AnyContent] =
    (identify andThen getData()).async { implicit request =>
      val cachedFileDetails = stubService.getCachedFileDetails(
        request.carfId,
        request.userAnswers,
        uploadId
      ) // TODO getCachedFileDetails will be reworked when backend repository implementation is complete (CARF-621)

      if (cachedFileDetails.fileStatus == Passed) {
        cachedFileDetails.dateTime
          .fold(Future.successful(recovery("Missing success datetime in cached file details"))) { datetime =>
            cachedFileDetails.extractedFileDetails.fold(
              Future.successful(recovery("Missing ExtractedFileDetails in cached file details"))
            ) { extractedFileDetails =>
              request.userAnswers.fold(
                Future.successful(prepareView(cachedFileDetails, extractedFileDetails, datetime))
              ) { userAnswers =>
                for {
                  updatedUserAnswers <- Future.fromTry(userAnswers.set(UploadCompletionLockPage, true))
                  _                  <- sessionRepository.set(updatedUserAnswers)
                } yield prepareView(cachedFileDetails, extractedFileDetails, datetime)
              }
            }
          }
      } else {
        Future.successful(recovery(s"The file with upload id ($uploadId) has not passed automated checks"))
      }
    }

  private def prepareView(
      cachedFileDetails: CachedFileDetails,
      extractedFileDetails: ExtractedFileDetails,
      datetime: LocalDateTime
  )(implicit request: Request[_], messages: Messages): Result = {
    val userEmailAddresses = cachedFileDetails.subscriptionDetails.getEmails
    val rcaspDetails       = cachedFileDetails.rcaspDetails
    val summaryListRows    = helper.rows(extractedFileDetails, rcaspDetails.getName)

    val summary = SummaryListViewModel(rows = summaryListRows)

    val formattedDate       = DateTimeFormats.dateTimeToString(datetime)
    val isRcaspUser         = rcaspDetails.IsRCASPUser
    val rcaspEmailAddresses = rcaspDetails.getEmails
    val emailAddressHtml    = generateEmailAddressHtml(userEmailAddresses, isRcaspUser, rcaspEmailAddresses)

    Ok(view(summary, formattedDate, config.managementUrl, emailAddressHtml))
  }

  private def generateEmailAddressHtml(
      emailAddresses: List[String],
      isRCASPUser: Boolean,
      rcaspEmailAddresses: List[String]
  )(implicit messages: Messages): String =
    emailAddresses match {
      case primary :: secondary :: Nil if isRCASPUser =>
        messages("fileConfirmation.2.email.sent", primary, secondary)
      case primary :: Nil if isRCASPUser              =>
        messages("fileConfirmation.1.email.sent", primary)
      case _                                          =>
        val flEmailAddresses       = rcaspEmailAddresses
        val completeEmailAddresses = emailAddresses ++ flEmailAddresses

        val emailToApplyComma = completeEmailAddresses.take(completeEmailAddresses.size - 1)
        val lastEmail         = completeEmailAddresses.last
        messages("fileConfirmation.2.email.sent", emailToApplyComma.mkString(", "), lastEmail)
    }
}
