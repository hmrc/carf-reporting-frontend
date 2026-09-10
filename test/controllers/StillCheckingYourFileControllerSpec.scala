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
import config.FrontendAppConfig
import models.errors.ApiError.InternalServerError
import models.fileSubmission.FileStatus.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalactic.Prettifier.default
import pages.{ExtractedFileDetailsPage, RcaspDetailsPage, UploadIdPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.XmlFileDetailsStubService
import types.ResultT
import utils.StillCheckingYourFileHelper
import views.html.StillCheckingYourFileView

class StillCheckingYourFileControllerSpec extends SpecBase {

  val mockStubService: XmlFileDetailsStubService                   = mock[XmlFileDetailsStubService]
  val mockStillCheckingYourFileHelper: StillCheckingYourFileHelper = mock[StillCheckingYourFileHelper]
  val mockAppConfig: FrontendAppConfig                             = mock[FrontendAppConfig]

  lazy val stillCheckingYourFileRoute: String = controllers.routes.StillCheckingYourFileController.onPageLoad().url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStubService, mockStillCheckingYourFileHelper)
  }

  "StillCheckingYourFile Controller" - {

    "must return OK and the correct view for a GET when file status is Pending (organisation, registered business)" in {
      when(mockAppConfig.managementUrl) thenReturn "managementUrl"
      when(mockAppConfig.feedbackUrl(any())) thenReturn "feedbackUrl"

      when(mockStubService.getFileStatus(any(), any())(any())).thenReturn(ResultT.fromValue(Pending))

      when(mockStillCheckingYourFileHelper.stillCheckingYourFileSummaryList(any())(any())).thenReturn(testSummaryList)

      val userAnswers = emptyUserAnswers
        .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)
        .withPage(UploadIdPage, testUploadId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[StillCheckingYourFileHelper].toInstance(mockStillCheckingYourFileHelper),
          bind[XmlFileDetailsStubService].toInstance(mockStubService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[StillCheckingYourFileView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          testSummaryList,
          "managementUrl",
          isRcaspUser = true,
          testRcaspName
        )(request, messages(application)).toString

        verify(mockStubService, times(1)).getFileStatus(any(), eqTo(userAnswers))(any())
        verify(mockStillCheckingYourFileHelper, times(1))
          .stillCheckingYourFileSummaryList(eqTo(testMessageRefId))(any())
      }
    }

    "must return OK and the correct view for a GET when file status is Pending (individual)" in {
      when(mockAppConfig.managementUrl) thenReturn "managementUrl"
      when(mockAppConfig.feedbackUrl(any())) thenReturn "feedbackUrl"

      when(mockStubService.getFileStatus(any(), any())(any())).thenReturn(ResultT.fromValue(Pending))

      when(mockStillCheckingYourFileHelper.stillCheckingYourFileSummaryList(any())(any())).thenReturn(testSummaryList)

      val userAnswers = emptyUserAnswers
        .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        .withPage(RcaspDetailsPage, individualRcaspDetails)
        .withPage(UploadIdPage, testUploadId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[StillCheckingYourFileHelper].toInstance(mockStillCheckingYourFileHelper),
          bind[XmlFileDetailsStubService].toInstance(mockStubService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[StillCheckingYourFileView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          testSummaryList,
          "managementUrl",
          isRcaspUser = false,
          "Nemona Champion"
        )(request, messages(application)).toString

        verify(mockStubService, times(1)).getFileStatus(any(), eqTo(userAnswers))(any())
        verify(mockStillCheckingYourFileHelper, times(1))
          .stillCheckingYourFileSummaryList(eqTo(testMessageRefId))(any())
      }
    }

    "must redirect to FilePassedChecks when file status is Passed" in {
      when(mockStubService.getFileStatus(any(), any())(any())).thenReturn(ResultT.fromValue(Passed))

      val userAnswers = emptyUserAnswers
        .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)
        .withPage(UploadIdPage, testUploadId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[StillCheckingYourFileHelper].toInstance(mockStillCheckingYourFileHelper),
          bind[XmlFileDetailsStubService].toInstance(mockStubService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.FilePassedChecksController.onPageLoad().url

        verify(mockStubService, times(1)).getFileStatus(any(), eqTo(userAnswers))(any())
        verify(mockStillCheckingYourFileHelper, times(0)).stillCheckingYourFileSummaryList(any())(any())
      }
    }

    "must redirect to FileFailedChecks when file status is Failed" in {
      when(mockStubService.getFileStatus(any(), any())(any())).thenReturn(ResultT.fromValue(Failed))

      val userAnswers = emptyUserAnswers
        .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)
        .withPage(UploadIdPage, testUploadId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[StillCheckingYourFileHelper].toInstance(mockStillCheckingYourFileHelper),
          bind[XmlFileDetailsStubService].toInstance(mockStubService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.FileFailedChecksController.onPageLoad().url

        verify(mockStubService, times(1)).getFileStatus(any(), eqTo(userAnswers))(any())
        verify(mockStillCheckingYourFileHelper, times(0)).stillCheckingYourFileSummaryList(any())(any())
      }
    }

    "must redirect to VirusFound when file status is VirusFound" in {
      when(mockStubService.getFileStatus(any(), any())(any())).thenReturn(ResultT.fromValue(VirusFound))

      val userAnswers = emptyUserAnswers
        .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)
        .withPage(UploadIdPage, testUploadId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[StillCheckingYourFileHelper].toInstance(mockStillCheckingYourFileHelper),
          bind[XmlFileDetailsStubService].toInstance(mockStubService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.VirusFoundController
          .onPageLoad(testUploadId.value)
          .url

        verify(mockStubService, times(1)).getFileStatus(any(), eqTo(userAnswers))(any())
        verify(mockStillCheckingYourFileHelper, times(0)).stillCheckingYourFileSummaryList(any())(any())
      }
    }

    "must redirect to FileNotAccepted when file status is UnprocessableErrorFile" in {
      when(mockStubService.getFileStatus(any(), any())(any())).thenReturn(ResultT.fromValue(UnprocessableErrorFile))

      val userAnswers = emptyUserAnswers
        .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)
        .withPage(UploadIdPage, testUploadId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[StillCheckingYourFileHelper].toInstance(mockStillCheckingYourFileHelper),
          bind[XmlFileDetailsStubService].toInstance(mockStubService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.PlaceholderController
          .onPageLoad("Should redirect to /problem/file-not-accepted (ticket TBC)")
          .url

        verify(mockStubService, times(1)).getFileStatus(any(), eqTo(userAnswers))(any())
        verify(mockStillCheckingYourFileHelper, times(0)).stillCheckingYourFileSummaryList(any())(any())
      }
    }

    "must redirect to Journey Recovery when file status is UnexpectedError" in {
      when(mockStubService.getFileStatus(any(), any())(any())).thenReturn(ResultT.fromValue(UnexpectedError))

      val userAnswers = emptyUserAnswers
        .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)
        .withPage(UploadIdPage, testUploadId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[StillCheckingYourFileHelper].toInstance(mockStillCheckingYourFileHelper),
          bind[XmlFileDetailsStubService].toInstance(mockStubService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockStubService, times(1)).getFileStatus(any(), eqTo(userAnswers))(any())
        verify(mockStillCheckingYourFileHelper, times(0)).stillCheckingYourFileSummaryList(any())(any())
      }
    }

    "must redirect to Journey Recovery when there is an error getting the file status" in {
      when(mockStubService.getFileStatus(any(), any())(any())).thenReturn(ResultT.fromError(InternalServerError))

      val userAnswers = emptyUserAnswers
        .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)
        .withPage(UploadIdPage, testUploadId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[StillCheckingYourFileHelper].toInstance(mockStillCheckingYourFileHelper),
          bind[XmlFileDetailsStubService].toInstance(mockStubService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockStubService, times(1)).getFileStatus(any(), eqTo(userAnswers))(any())
        verify(mockStillCheckingYourFileHelper, times(0)).stillCheckingYourFileSummaryList(any())(any())
      }
    }

    "must redirect to Journey Recovery when RcaspDetails is missing from user answers" in {
      val userAnswers = emptyUserAnswers
        .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        .withPage(UploadIdPage, testUploadId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[StillCheckingYourFileHelper].toInstance(mockStillCheckingYourFileHelper),
          bind[XmlFileDetailsStubService].toInstance(mockStubService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockStubService, times(0)).getFileStatus(any(), any())(any())
        verify(mockStillCheckingYourFileHelper, times(0)).stillCheckingYourFileSummaryList(any())(any())
      }
    }

    "must redirect to Journey Recovery for a GET when ExtractedFileDetails is missing from user answers" in {
      val userAnswers = emptyUserAnswers
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)
        .withPage(UploadIdPage, testUploadId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[StillCheckingYourFileHelper].toInstance(mockStillCheckingYourFileHelper),
          bind[XmlFileDetailsStubService].toInstance(mockStubService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockStubService, times(0)).getFileStatus(any(), any())(any())
        verify(mockStillCheckingYourFileHelper, times(0)).stillCheckingYourFileSummaryList(any())(any())
      }
    }

    "must redirect to Journey Recovery for a GET when UploadId is missing from user answers" in {
      val userAnswers = emptyUserAnswers
        .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[StillCheckingYourFileHelper].toInstance(mockStillCheckingYourFileHelper),
          bind[XmlFileDetailsStubService].toInstance(mockStubService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockStubService, times(0)).getFileStatus(any(), any())(any())
        verify(mockStillCheckingYourFileHelper, times(0)).stillCheckingYourFileSummaryList(any())(any())
      }
    }

    "must redirect to Journey Recovery for a GET when user answers do not exist" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, stillCheckingYourFileRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
