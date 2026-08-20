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

import connectors.{RcaspRegistrationConnector, SubscriptionConnector}
import controllers.actions.*
import pages.{ExtractedFileDetailsPage, RcaspDetailsPage, SubscriptionDetailsPage}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RcaspAndSubscriptionDetailsController @Inject() (
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    sessionRepository: SessionRepository,
    rcaspRegistrationConnector: RcaspRegistrationConnector,
    subscriptionConnector: SubscriptionConnector,
    val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {

  def onPageLoad(): Action[AnyContent] =
    (identify() andThen getData() andThen requireData).async { implicit request =>
      request.userAnswers
        .get(ExtractedFileDetailsPage)
        .fold {
          logWarn("[RcaspAndSubscriptionDetailsController][onPageLoad] Missing ExtractedFileDetails in user answers")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        } { extractedFileDetails =>
          rcaspRegistrationConnector.viewRcasps(request.carfId).value.flatMap {
            case Left(error)      =>
              logWarn(s"[RcaspAndSubscriptionDetailsController][onPageLoad] Error calling viewRcasps: $error")
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            case Right(rcaspList) =>
              rcaspList
                .find(_.RCASPID.equalsIgnoreCase(extractedFileDetails.sendingEntityIn))
                .fold {
                  logWarn(
                    s"[RcaspAndSubscriptionDetailsController][onPageLoad] sendingEntityIn from file does not match any of the user's RCASPs"
                  )
                  Future.successful(Redirect(controllers.problem.routes.RcaspNotMatchingController.onPageLoad()))
                } { matchingRcasp =>
                  subscriptionConnector.displaySubscription(request.carfId).value.flatMap {
                    case Left(error)                 =>
                      logWarn(
                        s"[RcaspAndSubscriptionDetailsController][onPageLoad] Error getting subscription data: $error"
                      )
                      Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                    case Right(subscriptionResponse) =>
                      subscriptionResponse.toSubscriptionDetails.fold {
                        logWarn(
                          "[RcaspAndSubscriptionDetailsController][onPageLoad] Unable to create SubscriptionDetails from DisplaySubscriptionResponse"
                        )
                        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                      } { subscriptionDetails =>
                        for {
                          updatedAnswers1 <- Future.fromTry(request.userAnswers.set(RcaspDetailsPage, matchingRcasp))
                          updatedAnswers2 <-
                            Future.fromTry(updatedAnswers1.set(SubscriptionDetailsPage, subscriptionDetails))
                          _               <- sessionRepository.set(updatedAnswers2)
                        } yield Redirect(controllers.routes.CheckYourFileDetailsController.onPageLoad())
                      }
                  }
                }
          }
        }
    }
}
