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

package testOnly.controllers

import connectors.RcaspRegistrationConnector
import controllers.actions.*
import models.UserAnswers
import pages.{RcaspDetailsPage, SendingEntityInPage}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/*
 * TODO: Remove/replace once real schema validation/data extraction is wired up (CARF-596). ExtractedFileDetails will be saved in user answers containing sendingEntityIn and SendingEntityInPage can be removed.
 * TODO: It is also at this stage (after XML validation) that we get subscription data and save it in user answers (CARF-625) - add it here if needed before CARF-596
 * This controller is a test-only stub (see conf/testOnlyDoNotUseInAppConf.routes) that
 * simulates the SendingEntityIN value that will eventually be returned from the backend
 * after successful schema validation of the uploaded file.
 */
class RcaspValidationController @Inject() (
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    sessionRepository: SessionRepository,
    rcaspRegistrationConnector: RcaspRegistrationConnector,
    val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  def onPageLoad(sendingEntityIn: Option[String]): Action[AnyContent] =
    (identify() andThen getData()).async { implicit request =>
      sendingEntityIn match {
        case None =>
          logger.warn("[RcaspValidationController][onPageLoad] Unable to extract SendingEntityIN from file")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Some(value) =>
          rcaspRegistrationConnector.viewRcasps(request.carfId).value.flatMap {
            case Left(error) =>
              logger.warn(s"[RcaspValidationController][onPageLoad] Error calling viewRcasps: $error")
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

            case Right(rcaspList) =>
              val existingAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))
              rcaspList
                .find(_.RCASPID.equalsIgnoreCase(value))
                .fold {
                  for {
                    updatedAnswers <- Future.fromTry(existingAnswers.set(SendingEntityInPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(controllers.problem.routes.RcaspNotMatchingController.onPageLoad())
                } { matchingRcasp =>
                  for {
                    updatedAnswers1 <- Future.fromTry(existingAnswers.set(SendingEntityInPage, value))
                    updatedAnswers2 <-
                      Future.fromTry(updatedAnswers1.set(RcaspDetailsPage, matchingRcasp))
                    _               <- sessionRepository.set(updatedAnswers2)
                  } yield Redirect(controllers.routes.CheckYourFileDetailsController.onPageLoad())
                }
          }
      }
    }
}
