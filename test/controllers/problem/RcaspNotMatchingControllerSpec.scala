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
import pages.ExtractedFileDetailsPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.problem.RcaspNotMatchingView

class RcaspNotMatchingControllerSpec extends SpecBase {

  "RcaspNotMatchingController" - {

    "must return OK and the correct view for a GET when ExtractedFileDetails is present in user answers" in {
      val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RcaspNotMatchingController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[RcaspNotMatchingView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(testRcaspId)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET when no user answers exist" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.RcaspNotMatchingController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when ExtractedFileDetails is missing from user answers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RcaspNotMatchingController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
