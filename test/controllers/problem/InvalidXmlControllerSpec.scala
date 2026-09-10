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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.UploadSuccessDetailsPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.problem.InvalidXmlView

class InvalidXmlControllerSpec extends SpecBase {

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  lazy val invalidXmlRoute: String = controllers.problem.routes.InvalidXmlController.onPageLoad().url

  "InvalidXml Controller" - {

    "must return OK and the correct view for a GET" in {
      when(mockAppConfig.managementUrl) thenReturn "managementUrl"
      when(mockAppConfig.feedbackUrl(any())) thenReturn "feedbackUrl"

      val userAnswers = emptyUserAnswers.withPage(UploadSuccessDetailsPage, uploadSuccessDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
        .build()

      running(application) {
        val request = FakeRequest(GET, invalidXmlRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[InvalidXmlView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(testFileName, "managementUrl")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET when user answers do not contain UploadSuccessDetails" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, invalidXmlRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when user answers do not exist" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, invalidXmlRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
