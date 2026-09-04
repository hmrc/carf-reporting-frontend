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

package controllers.problem

import base.SpecBase
import config.FrontendAppConfig
import models.problem.SchemaError
import pages.{UploadSuccessDetailsPage, XmlErrorsPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import views.html.problem.DataErrorsView

class DataErrorsControllerSpec extends SpecBase {

  lazy val dataErrorsRoute: String = controllers.problem.routes.DataErrorsController.onPageLoad().url

  "DataErrors Controller" - {

    "must return OK and the correct view when errors and filename are both present, under the max" in {
      val userAnswers = emptyUserAnswers
        .withPage(UploadSuccessDetailsPage, uploadSuccessDetails)
        .withPage(XmlErrorsPage, xmlFewErrors)

      val schemaErrors = xmlFewErrors.map { error =>
        SchemaError(error.lineNumber, Text(error.errorMessage).asHtml)
      }

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, dataErrorsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DataErrorsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(testFileName, schemaErrors, hasMoreThanMax = false, appConfig.managementUrl)(
            request,
            messages(application)
          ).toString
      }
    }

    "must return OK and truncate to 100 rows with hasMoreThanMax true when errors exceed the max" in {
      val userAnswers = emptyUserAnswers
        .withPage(UploadSuccessDetailsPage, uploadSuccessDetails)
        .withPage(XmlErrorsPage, xmlManyErrors)

      val schemaErrors = xmlManyErrors.map { error =>
        SchemaError(error.lineNumber, Text(error.errorMessage).asHtml)
      }

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, dataErrorsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DataErrorsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(testFileName, schemaErrors.take(100), hasMoreThanMax = true, appConfig.managementUrl)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to Journey Recovery when both errors and filename are missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dataErrorsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when filename is missing but errors are present" in {
      val userAnswers = emptyUserAnswers.withPage(XmlErrorsPage, xmlFewErrors)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dataErrorsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when errors are empty but filename is present" in {
      val userAnswers = emptyUserAnswers.withPage(UploadSuccessDetailsPage, uploadSuccessDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dataErrorsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when user answers do not exist" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, dataErrorsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
