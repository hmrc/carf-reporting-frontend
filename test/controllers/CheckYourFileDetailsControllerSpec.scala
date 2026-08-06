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

import base.SpecBase
import models.ExtractedFileDetails
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import pages.{ExtractedFileDetailsPage, RcaspDetailsPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.CheckYourFileDetailsHelper
import views.html.CheckYourFileDetailsView

class CheckYourFileDetailsControllerSpec extends SpecBase {

  val mockCheckYourFileDetailsHelper: CheckYourFileDetailsHelper = mock[CheckYourFileDetailsHelper]

  lazy val checkFileDetailsRoute: String = controllers.routes.CheckYourFileDetailsController.onPageLoad().url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCheckYourFileDetailsHelper)
  }

  "CheckYourFileDetails Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

      when(mockCheckYourFileDetailsHelper.fileDetailsSummaryList(any())(any())).thenReturn(testSummaryList)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[CheckYourFileDetailsHelper].toInstance(mockCheckYourFileDetailsHelper))
        .build()

      running(application) {
        val request = FakeRequest(GET, checkFileDetailsRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[CheckYourFileDetailsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(testRcaspName, testSummaryList)(request, messages(application)).toString

        verify(mockCheckYourFileDetailsHelper, times(1)).fileDetailsSummaryList(eqTo(extractedFileDetailsTestData))(
          any()
        )
      }
    }

    "must redirect to Journey Recovery for a GET when RcaspDetails is missing from user answers" in {
      val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[CheckYourFileDetailsHelper].toInstance(mockCheckYourFileDetailsHelper))
        .build()

      running(application) {
        val request = FakeRequest(GET, checkFileDetailsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockCheckYourFileDetailsHelper, times(0)).fileDetailsSummaryList(any())(any())
      }
    }

    "must redirect to Journey Recovery for a GET when ExtractedFileDetails is missing from user answers" in {
      val userAnswers = emptyUserAnswers.withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[CheckYourFileDetailsHelper].toInstance(mockCheckYourFileDetailsHelper))
        .build()

      running(application) {
        val request = FakeRequest(GET, checkFileDetailsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockCheckYourFileDetailsHelper, times(0)).fileDetailsSummaryList(any())(any())
      }
    }

    "must redirect to Journey Recovery for a GET when RcaspDetails and ExtractedFileDetails are missing from user answers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CheckYourFileDetailsHelper].toInstance(mockCheckYourFileDetailsHelper))
        .build()

      running(application) {
        val request = FakeRequest(GET, checkFileDetailsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockCheckYourFileDetailsHelper, times(0)).fileDetailsSummaryList(any())(any())
      }
    }

    "must redirect to Journey Recovery for a GET when user answers do not exist" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, checkFileDetailsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
