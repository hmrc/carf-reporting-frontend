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
import models.upscan.UploadId
import play.api.data.Form

import javax.inject.Inject
import org.apache.pekko
import org.apache.pekko.actor.ActorSystem
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.upload.UploadXmlView

import scala.concurrent.duration.DurationInt

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
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad: Action[AnyContent] = (identify() andThen getData()) { implicit request =>
    Ok(view(form))
  }

  def sleep: Action[AnyContent] = (identify() andThen getData()) { implicit request =>
    Thread.sleep(5000)
    Ok(view(form))
  }

//  def onSubmit(): Action[AnyContent] =
//    (identify() andThen getData() andThen requireData).async { implicit request =>
//
//    }
//

//  def getStatus(uploadId: UploadId): Action[AnyContent] = (identify() andThen getData() andThen requireData).async {
//    implicit request =>
//      // Delay the call to make sure the backend db has been populated by the upscan callback first
//      pekko.pattern.after(config.upscanCallbackDelayInSeconds.seconds, actorSystem.scheduler) {
//        upscanConnector.getUploadStatus(uploadId) map {
//          case Some(uploadedSuccessfully: UploadedSuccessfully) =>
//            if (isFileNameInValid(uploadedSuccessfully.name)) {
//              Redirect(routes.IndexController.showError(InvalidArgument.code, InvalidFileNameLength.message, "").url)
//            } else if (isFileEmpty(uploadedSuccessfully.size)) {
//              Redirect(routes.IndexController.showError(InvalidArgument.code, FileIsEmpty.message, "").url)
//            } else {
//              Redirect(routes.FileValidationController.onPageLoad().url)
//            }
//          case Some(r: UploadRejected)                          =>
//            if (r.details.message.contains("octet-stream")) {
//              logger.warn(s"Show errorForm on rejection $r")
//              val errorReason = r.details.failureReason
//              Redirect(routes.IndexController.showError(OctetStream.code, errorReason.toLowerCase, "").url)
//            } else {
//              logger.warn(s"Upload rejected. Error details: ${r.details}")
//              Redirect(routes.IndexController.showError(InvalidArgument.code, TypeMismatch.message, "").url)
//            }
//          case Some(Quarantined)                                =>
//            Redirect(routes.IndexController.showError(VirusFile.code, "", "").url)
//          case Some(Failed)                                     =>
//            logger.warn("File upload returned failed status")
//            Redirect(routes.IndexController.showError("UploadFailed", "", "").url)
//          case Some(_)                                          =>
//            Redirect(routes.IndexController.getStatus(uploadId).url)
//          case None                                             =>
//            logger.error("Unable to retrieve file upload status from Upscan")
//            Redirect(routes.IndexController.showError("UploadFailed", "", "").url)
//        }
//      }
//  }

  private def isFileNameInValid(name: String): Boolean =
    name.stripSuffix(".xml").length > config.upscanMaxFileNameLength

  private def isFileEmpty(size: Long): Boolean = size == 0L
}
