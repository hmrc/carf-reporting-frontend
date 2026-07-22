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

import config.FrontendAppConfig
import controllers.actions.*
import forms.UploadXmlFormProvider
import models.ErrorCode.{InvalidArgument, OctetStream, VirusFile}
import models.InvalidArgumentErrorMessage.{DisallowedCharacters, FileIsEmpty, InvalidFileNameLength, TypeMismatch}
import models.upscan.{Reference, UpscanInitiateResponse}
import models.{ErrorCode, InvalidArgumentErrorMessage}
import org.apache.pekko
import org.apache.pekko.actor.ActorSystem
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.upload.UploadXmlView

import javax.inject.Inject
import scala.concurrent.Future

class UploadXmlController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    actorSystem: ActorSystem,
    config: FrontendAppConfig,
    formProvider: UploadXmlFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: UploadXmlView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  // TODO: Remove when implementing Upscan functionality (CARF-578, CARF-579)
  val upscanInitiateResponse = UpscanInitiateResponse(
    fileReference = Reference("abc"),
    postTarget = "http://localhost:17004/send-a-cryptoasset-report/report/sleep",
    formFields = Map.empty
  )

  val form: Form[String] = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify() andThen getData()) { implicit request =>
    Ok(view(form, upscanInitiateResponse))
  }

  // TODO: Remove when implementing Upscan functionality (CARF-578, CARF-579)
  def sleep(): Action[AnyContent] = (identify() andThen getData()) { implicit request =>
    Thread.sleep(5000)
    Ok(view(form, upscanInitiateResponse))
  }

  def showError(errorCode: String, errorMessage: String, errorRequestId: String): Action[AnyContent] =
    (identify() andThen getData()).async { implicit request =>

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
          logger.warn(s"Upscan error $errorCode: $errorMessage, requestId is $errorRequestId")
          form.withError("file-upload", "uploadXml.error.file.content.unknown")
      }
      Future.successful(Ok(view(formWithErrors, upscanInitiateResponse)))
    }
}
