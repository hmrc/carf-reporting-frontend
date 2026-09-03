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

import connectors.FileValidationConnector
import controllers.actions.*
import models.errors.{InvalidXmlError, XmlErrors}
import pages.{ExtractedFileDetailsPage, UploadSuccessDetailsPage, XmlErrorsPage}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileValidationController @Inject() (
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    uploadCompletionLock: UploadCompletionLockAction,
    requireData: DataRequiredAction,
    sessionRepository: SessionRepository,
    fileValidationConnector: FileValidationConnector,
    val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData() andThen uploadCompletionLock andThen requireData).async { implicit request =>
      request.userAnswers
        .get(UploadSuccessDetailsPage)
        .fold {
          logWarn("[FileValidationController][onPageLoad] Missing UploadSuccessDetails from user answers")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        } { uploadSuccessDetails =>
          fileValidationConnector.validateUploadedFile(uploadSuccessDetails.downloadUrl).value.flatMap {
            case Right(extractedFileDetails) =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.set(ExtractedFileDetailsPage, extractedFileDetails))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.routes.RcaspAndSubscriptionDetailsController.onPageLoad())
            case Left(InvalidXmlError)       =>
              logWarn("[FileValidationController][onPageLoad] Received InvalidXmlError from file validation")
              Future.successful(Redirect(controllers.problem.routes.InvalidXmlController.onPageLoad()))
            case Left(XmlErrors(xmlErrors))  =>
              logWarn("[FileValidationController][onPageLoad] Received schema validation errors from file validation")
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(XmlErrorsPage, xmlErrors))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.problem.routes.DataErrorsController.onPageLoad())
            case Left(error)                 =>
              logWarn(s"[FileValidationController][onPageLoad] Unexpected error from file validation: $error")
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
        }
    }
}
