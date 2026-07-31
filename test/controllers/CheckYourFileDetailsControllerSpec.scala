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
import models.DocTypeIndic.OECD10
import models.ExtractedFileDetails
import models.MessageTypeIndic.CARF701
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import pages.{RcaspDetailsPage, SendingEntityInPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import service.StubService
import utils.CheckYourFileDetailsHelper
import views.html.CheckYourFileDetailsView

class CheckYourFileDetailsControllerSpec extends SpecBase {

  // TODO: Remove StubService when file validation and data extraction are linked to the frontend (CARF-596)
  val mockStubService: StubService                               = mock[StubService]
  val mockCheckYourFileDetailsHelper: CheckYourFileDetailsHelper = mock[CheckYourFileDetailsHelper]

  lazy val checkFileDetailsRoute: String = controllers.routes.CheckYourFileDetailsController.onPageLoad().url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStubService, mockCheckYourFileDetailsHelper)
  }

  "CheckYourFileDetails Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .withPage(SendingEntityInPage, testRcaspId)
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

      val testExtractedFileDetails = ExtractedFileDetails(
        messageRefId = testMessageRefId,
        sendingEntityIn = testRcaspId,
        rcaspName = Some(testRcaspName),
        messageTypeIndic = CARF701,
        hasOtherNexus = false,
        hasCryptoUsers = true,
        docTypeIndic = OECD10,
        isTestData = true,
        allCryptoUsersAreCorrections = false,
        allCryptoUsersAreDeletions = false
      )

      when(mockStubService.getExtractedFileDetails(any(), any())).thenReturn(Some(testExtractedFileDetails))
      when(mockCheckYourFileDetailsHelper.fileDetailsSummaryList(any())(any())).thenReturn(testSummaryList)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[StubService].toInstance(mockStubService),
          bind[CheckYourFileDetailsHelper].toInstance(mockCheckYourFileDetailsHelper)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, checkFileDetailsRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[CheckYourFileDetailsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(testRcaspName, testSummaryList)(request, messages(application)).toString

        verify(mockStubService, times(1)).getExtractedFileDetails(any(), eqTo(testRcaspId))
        verify(mockCheckYourFileDetailsHelper, times(1)).fileDetailsSummaryList(eqTo(testExtractedFileDetails))(any())
      }
    }

    // TODO: Could be removed in CARF-596 if SendingEntityIn is already saved as part of ExtractedFileDetails after data extraction
    "must redirect to Journey Recovery for a GET when SendingEntityIn is missing from user answers" in {
      val userAnswers = emptyUserAnswers
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[StubService].toInstance(mockStubService),
          bind[CheckYourFileDetailsHelper].toInstance(mockCheckYourFileDetailsHelper)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, checkFileDetailsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockStubService, times(0)).getExtractedFileDetails(any(), any())
        verify(mockCheckYourFileDetailsHelper, times(0)).fileDetailsSummaryList(any())(any())
      }
    }

    "must redirect to Journey Recovery for a GET when RcaspDetails is missing from user answers" in {
      val userAnswers = emptyUserAnswers.withPage(SendingEntityInPage, testRcaspId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[StubService].toInstance(mockStubService),
          bind[CheckYourFileDetailsHelper].toInstance(mockCheckYourFileDetailsHelper)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, checkFileDetailsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockStubService, times(0)).getExtractedFileDetails(any(), any())
        verify(mockCheckYourFileDetailsHelper, times(0)).fileDetailsSummaryList(any())(any())
      }
    }

    "must redirect to Journey Recovery for a GET when ExtractedFileDetails is missing from user answers" in {
      val userAnswers = emptyUserAnswers
        .withPage(SendingEntityInPage, testRcaspId)
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

      when(mockStubService.getExtractedFileDetails(any(), any())).thenReturn(None)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[StubService].toInstance(mockStubService),
          bind[CheckYourFileDetailsHelper].toInstance(mockCheckYourFileDetailsHelper)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, checkFileDetailsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockStubService, times(1)).getExtractedFileDetails(any(), eqTo(testRcaspId))
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
