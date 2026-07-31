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

import controllers.actions.*
import models.rcasp.getName
import pages.{RcaspDetailsPage, SendingEntityInPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.StubService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckYourFileDetailsHelper
import views.html.CheckYourFileDetailsView

import javax.inject.Inject

class CheckYourFileDetailsController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    stubService: StubService,
    checkYourFileDetailsHelper: CheckYourFileDetailsHelper,
    view: CheckYourFileDetailsView,
    val controllerComponents: MessagesControllerComponents
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify() andThen getData() andThen requireData) { implicit request =>
    val userAnswers = request.userAnswers

    // TODO: Get ExtractedFileDetails from user answers once file validation/data extraction has been linked to frontend (CARF-596)
    (for {
      sendingEntityIn      <- userAnswers.get(SendingEntityInPage)
      rcaspDetails         <- userAnswers.get(RcaspDetailsPage)
      extractedFileDetails <- stubService.getExtractedFileDetails(request.carfId, sendingEntityIn)
    } yield {
      val fileDetailsSummaryList = checkYourFileDetailsHelper.fileDetailsSummaryList(extractedFileDetails)
      Ok(view(rcaspDetails.getName, fileDetailsSummaryList))
    }).getOrElse {
      logger.warn(
        "[CheckYourFileDetailsController][onPageLoad] Unable to get RCASP details or ExtractedFileDetails from user answers"
      )
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
