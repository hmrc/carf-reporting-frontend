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

import controllers.actions.IdentifierAction
import forms.UploadXMLFormProvider
import models.upscan.{Reference, UpscanInitiateResponse}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{IndexView, UploadXMLView}

import javax.inject.Inject

class IndexController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    identify: IdentifierAction,
    formProvider: UploadXMLFormProvider,
    view: UploadXMLView
) extends FrontendBaseController
    with I18nSupport {

  val upscanInitiateResponse = UpscanInitiateResponse(
    fileReference = Reference("abc"),
    postTarget = "http://localhost:17004/send-a-cryptoasset-report/report/sleep",
    formFields = Map.empty
  )

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = identify() { implicit request =>
    Ok(view(form, upscanInitiateResponse))
  }

  def sleep(): Action[AnyContent] = identify() { implicit request =>
    Thread.sleep(5000)
    println("AAAAAAAAAAAA")
    Ok(view(form, upscanInitiateResponse))
  }
}
