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

import config.Constants.fileNameAllowedCharacters
import config.FrontendAppConfig
import connectors.UpscanConnector
import controllers.actions.*
import forms.UploadXmlFormProvider
import models.ErrorCode.{InvalidArgument, OctetStream, VirusFile}
import models.InvalidArgumentErrorMessage.{DisallowedCharacters, FileIsEmpty, InvalidFileNameLength, TypeMismatch}
import models.requests.OptionalDataRequest
import models.upscan.*
import models.upscan.UploadStatus.*
import models.{ErrorCode, InvalidArgumentErrorMessage, UserAnswers}
import org.apache.pekko
import org.apache.pekko.actor.ActorSystem
import pages.{FileReferencePage, UploadIdPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.*
import views.html.upload.UploadXmlView

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class UploadXmlController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    upscanConnector: UpscanConnector,
    sessionRepository: SessionRepository,
    actorSystem: ActorSystem,
    config: FrontendAppConfig,
    formProvider: UploadXmlFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: UploadXmlView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData()).async { implicit request =>
    initialUpscanCall(form)
  }

  def showError(errorCode: String, errorMessage: String, errorRequestId: String): Action[AnyContent] =
    (identify andThen getData()).async { implicit request =>

      val formWithErrors: Form[String] = ErrorCode.fromCode(errorCode) match {
        case Some(ErrorCode.EntityTooLarge)      => form.withError("file-upload", "uploadXml.error.file.size.large")
        case Some(VirusFile)                     => form.withError("file-upload", "uploadXml.error.file.content.virus")
        case Some(InvalidArgument | OctetStream) =>
          InvalidArgumentErrorMessage.fromMessage(errorMessage) match {
            case Some(InvalidFileNameLength) => form.withError("file-upload", "uploadXml.error.file.name.length")
            case Some(DisallowedCharacters)  =>
              form.withError("file-upload", "uploadXml.error.file.name.disallowed.characters")
            case Some(TypeMismatch)          => form.withError("file-upload", "uploadXml.error.file.type.invalid")
            case Some(FileIsEmpty)           => form.withError("file-upload", "uploadXml.error.file.content.empty")
            case None                        => form.withError("file-upload", "uploadXml.error.file.select")
          }
        case _                                   =>
          logWarn(s"Upscan error $errorCode: $errorMessage, requestId is $errorRequestId")
          form.withError("file-upload", "uploadXml.error.file.content.unknown")
      }
      initialUpscanCall(formWithErrors)
    }

  private def initialUpscanCall(
      preparedForm: Form[String]
  )(implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier): Future[Result] = {
    val uploadId: UploadId = UploadId.generate
    val userAnswers        = request.userAnswers.getOrElse(UserAnswers(id = request.userId))

    upscanConnector.upscanFormInitiate(uploadId).value.flatMap {
      case Right(upscanInitiateResponse) =>
        upscanConnector.saveRequestedUpload(uploadId, upscanInitiateResponse.fileReference).value.flatMap {
          case Right(_)    =>
            for {
              answersWithUploadId <- Future.fromTry(userAnswers.set(UploadIdPage, uploadId))
              updatedAnswers      <-
                Future.fromTry(answersWithUploadId.set(FileReferencePage, upscanInitiateResponse.fileReference))
              _                   <- sessionRepository.set(updatedAnswers)
            } yield Ok(view(preparedForm, upscanInitiateResponse))
          case Left(error) =>
            logError(s"[UploadXmlController][initialUpscanCall] Error setting initial upload status: $error")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      case Left(error)                   =>
        logError(s"[UploadXmlController][initialUpscanCall] Error getting UpscanInitiateResponse: $error")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  def getUploadStatusAndRedirect(uploadId: UploadId): Action[AnyContent] =
    (identify andThen getData() andThen requireData).async { implicit request =>
      def errorRedirect(errorCode: String, errorMessage: String, errorRequestId: String): Result =
        Redirect(controllers.upload.routes.UploadXmlController.showError(errorCode, errorMessage, errorRequestId).url)

      // Delay the call to make sure the backend db has been populated by the upscan callback first
      pekko.pattern.after(config.upscanCallbackDelayInSeconds.seconds, actorSystem.scheduler) {
        upscanConnector.getUploadStatus(uploadId).value.map {
          case Right(maybeUploadStatus) =>
            maybeUploadStatus match {
              case Some(uploadedSuccessfully: UploadedSuccessfully) =>
                if (isFileNameTooLong(uploadedSuccessfully.name)) {
                  errorRedirect(InvalidArgument.code, InvalidFileNameLength.message, "")
                } else if (isFileNameDisallowed(uploadedSuccessfully.name)) {
                  errorRedirect(InvalidArgument.code, DisallowedCharacters.message, "")
                } else if (isFileNotXml(uploadedSuccessfully.name)) {
                  // When running locally, upscan stub uploads non-XML successfully. Actual Upscan would return an UploadRejected.
                  errorRedirect(InvalidArgument.code, TypeMismatch.message, "")
                } else if (isFileEmpty(uploadedSuccessfully.size)) {
                  errorRedirect(InvalidArgument.code, FileIsEmpty.message, "")
                } else {
                  Redirect(
                    controllers.routes.PlaceholderController
                      .onPageLoad("Upscan checks passed. Should redirect to FileValidationController (CARF-596)")
                      .url
                  )
                }
              case Some(uploadRejected: UploadRejected)             =>
                if (uploadRejected.details.message.contains("octet-stream")) {
                  logWarn(
                    s"[UploadXmlController][getUploadStatusAndRedirect] Upload rejected with 'octet-stream' in message. Error details: ${uploadRejected.details}"
                  )
                  val errorReason = uploadRejected.details.failureReason
                  errorRedirect(OctetStream.code, errorReason.toLowerCase, "")
                } else {
                  logWarn(
                    s"[UploadXmlController][getUploadStatusAndRedirect] Upload rejected. Error details: ${uploadRejected.details}"
                  )
                  errorRedirect(InvalidArgument.code, TypeMismatch.message, "")
                }
              case Some(Quarantined)                                =>
                errorRedirect(VirusFile.code, "", "")
              case Some(Failed)                                     =>
                logWarn("[UploadXmlController][getUploadStatusAndRedirect] File upload returned failed status")
                errorRedirect("UploadFailed", "", "")
              case Some(_)                                          =>
                Redirect(controllers.upload.routes.UploadXmlController.getUploadStatusAndRedirect(uploadId).url)
              case None                                             =>
                logError(
                  s"[UploadXmlController][getUploadStatusAndRedirect] Unable to retrieve a record with uploadId ${uploadId.value}"
                )
                errorRedirect("UploadFailed", "", "")
            }
          case Left(error)              =>
            logError(s"[UploadXmlController][getUploadStatusAndRedirect] Error getting upload status: $error")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

  private def isFileNameTooLong(name: String): Boolean =
    name.stripSuffix(".xml").length > config.upscanMaxFileNameLength

  private def isFileNameDisallowed(name: String): Boolean = !name.matches(fileNameAllowedCharacters)

  private def isFileNotXml(name: String): Boolean = !name.endsWith(".xml")

  private def isFileEmpty(size: Long): Boolean = size == 0L
}
