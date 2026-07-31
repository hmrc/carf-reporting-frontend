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

import controllers.actions._
import javax.inject.Inject
import models.problem.DataErrorsStubData
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.problem.DataErrorsView

class DataErrorsTestOnlyController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    val controllerComponents: MessagesControllerComponents,
    view: DataErrorsView
) extends FrontendBaseController
    with I18nSupport {

  private val fileName: String    = "filename.xml"
  private val maxErrorsShown: Int = 100

  def fewErrors(): Action[AnyContent] = identify() { implicit request =>
    val errors = DataErrorsStubData.fewErrors
    Ok(view(fileName, errors.take(maxErrorsShown), hasMoreThanMax = errors.size > maxErrorsShown))
  }

  def manyErrors(): Action[AnyContent] = identify() { implicit request =>
    Ok(view(fileName, DataErrorsStubData.manyErrors.take(100), hasMoreThanMax = true))
  }

}
