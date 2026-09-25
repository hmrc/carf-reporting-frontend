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
import connectors.{SDESConnector, SubmissionDetailsConnector}
import models.errors.ApiError.InternalServerError
import models.fileSubmission.FileStatus.*
import models.fileSubmission.URL
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import pages.*
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import types.ResultT
import views.html.SendYourFileView

class SendYourFileControllerSpec extends SpecBase {

  val mockAppConfig: FrontendAppConfig                           = mock[FrontendAppConfig]
  val mockSDESConnector: SDESConnector                           = mock[SDESConnector]
  val mockSubmissionDetailsConnector: SubmissionDetailsConnector = mock[SubmissionDetailsConnector]

  lazy val sendYourFileRoute: String       = routes.SendYourFileController.onPageLoad().url
  lazy val sendYourFileStatusRoute: String = routes.SendYourFileController.getFileStatusAndRedirect.url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSDESConnector, mockSubmissionDetailsConnector)
  }

  "SendYourFile Controller" - {

    ".onPageLoad" - {
      when(mockAppConfig.spinnerMaxPollingAttempts) thenReturn 10
      when(mockAppConfig.feedbackUrl(any())) thenReturn "feedbackUrl"

      "must return OK and the correct view with warning text" in {
        val userAnswers = emptyUserAnswers
          .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
          .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(
            Some("We cannot complete all checks on test data or accept the file."),
            maxPollingAttempts = 10
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view without warning text" in {
        val userAnswers = emptyUserAnswers
          .withPage(ExtractedFileDetailsPage, extractedFileDetailsNilReport)
          .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(None, maxPollingAttempts = 10)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery when RcaspDetails is missing from user answers" in {
        val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetailsNilReport)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when ExtractedFileDetails is missing from user answers" in {
        val userAnswers = emptyUserAnswers.withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when RcaspDetails and ExtractedFileDetails are missing from user answers" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when user answers do not exist" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    ".onSubmit" - {
      "must submit to FTS and redirect to StillCheckingYourFileController" in {
        when(mockSDESConnector.sendSubmission(any())(any(), any())).thenReturn(ResultT.fromValue(()))

        val userAnswers = emptyUserAnswers
          .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
          .withPage(SubscriptionDetailsPage, displaySubscriptionDetailsOrg)
          .withPage(RcaspDetailsPage, organisationStandardRcaspDetails)
          .withPage(UploadDetailsUserAnswers, uploadDetailsUserAnswers)
          .withPage(UploadIdPage, testUploadId)

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[SDESConnector].toInstance(mockSDESConnector)
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.StillCheckingYourFileController.onPageLoad().url
          verify(mockSDESConnector, times(1)).sendSubmission(any())(any(), any())
        }
      }

      "must redirect to Journey recovery when the SDES call fails" in {
        when(mockSDESConnector.sendSubmission(any())(any(), any()))
          .thenReturn(ResultT.fromError(InternalServerError))

        val userAnswers = emptyUserAnswers
          .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
          .withPage(SubscriptionDetailsPage, displaySubscriptionDetailsOrg)
          .withPage(RcaspDetailsPage, organisationStandardRcaspDetails)
          .withPage(UploadDetailsUserAnswers, uploadDetailsUserAnswers)
          .withPage(UploadIdPage, testUploadId)

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[SDESConnector].toInstance(mockSDESConnector)
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          verify(mockSDESConnector, times(1)).sendSubmission(any())(any(), any())
        }
      }

      "must redirect to Journey Recovery when ExtractedFileDetails is missing from user answers" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SDESConnector].toInstance(mockSDESConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          verify(mockSDESConnector, times(0)).sendSubmission(any())(any(), any())
        }
      }

      "must redirect to Journey Recovery when user answers do not exist" in {

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SDESConnector].toInstance(mockSDESConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          verify(mockSDESConnector, times(0)).sendSubmission(any())(any(), any())
        }
      }
    }

    ".getFileStatusAndRedirect" - {
      "must return NoContent when file status is Pending" in {
        when(mockSubmissionDetailsConnector.getFileStatus(any())(any(), any()))
          .thenReturn(ResultT.fromValue(Pending))

        val userAnswers = emptyUserAnswers.withPage(UploadIdPage, testUploadId)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileStatusRoute)
          val result  = route(application, request).value

          status(result) mustEqual NO_CONTENT

          verify(mockSubmissionDetailsConnector, times(1)).getFileStatus(eqTo(testUploadId))(any(), any())
        }
      }

      "must return OK with url to file confirmation page when file status is Passed" in {
        when(mockSubmissionDetailsConnector.getFileStatus(any())(any(), any()))
          .thenReturn(ResultT.fromValue(Passed))

        val userAnswers = emptyUserAnswers.withPage(UploadIdPage, testUploadId)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileStatusRoute)
          val result  = route(application, request).value

          status(result)        mustEqual OK
          contentAsJson(result) mustEqual Json.toJson(
            URL(controllers.routes.FileConfirmationController.onPageLoad(testUploadId.value).url)
          )

          verify(mockSubmissionDetailsConnector, times(1)).getFileStatus(eqTo(testUploadId))(any(), any())
        }
      }

      "must return OK with url to rules errors page when file status is Failed" in {
        when(mockSubmissionDetailsConnector.getFileStatus(any())(any(), any()))
          .thenReturn(ResultT.fromValue(Failed))

        val userAnswers = emptyUserAnswers.withPage(UploadIdPage, testUploadId)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileStatusRoute)
          val result  = route(application, request).value

          status(result)        mustEqual OK
          contentAsJson(result) mustEqual Json.toJson(
            URL(controllers.problem.routes.RulesErrorsController.onPageLoad(testUploadId.value).url)
          )

          verify(mockSubmissionDetailsConnector, times(1)).getFileStatus(eqTo(testUploadId))(any(), any())
        }
      }

      "must return OK with url to virus found page when file status is VirusFound" in {
        when(mockSubmissionDetailsConnector.getFileStatus(any())(any(), any()))
          .thenReturn(ResultT.fromValue(VirusFound))

        val userAnswers = emptyUserAnswers.withPage(UploadIdPage, testUploadId)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileStatusRoute)
          val result  = route(application, request).value

          status(result)        mustEqual OK
          contentAsJson(result) mustEqual Json.toJson(
            URL(controllers.problem.routes.VirusFoundController.onPageLoad(testUploadId.value).url)
          )

          verify(mockSubmissionDetailsConnector, times(1)).getFileStatus(eqTo(testUploadId))(any(), any())
        }
      }

      "must return OK with url to file-not-accepted when file status is UnprocessableErrorFile" in {
        when(mockSubmissionDetailsConnector.getFileStatus(any())(any(), any()))
          .thenReturn(ResultT.fromValue(UnprocessableErrorFile))

        val userAnswers = emptyUserAnswers.withPage(UploadIdPage, testUploadId)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileStatusRoute)
          val result  = route(application, request).value

          status(result)        mustEqual OK
          contentAsJson(result) mustEqual Json.toJson(
            URL(
              controllers.routes.PlaceholderController
                .onPageLoad("Should redirect to /problem/file-not-accepted (ticket TBC)")
                .url
            )
          )

          verify(mockSubmissionDetailsConnector, times(1)).getFileStatus(eqTo(testUploadId))(any(), any())
        }
      }

      "must return OK with url to journey recovery when file status is UnexpectedError" in {
        when(mockSubmissionDetailsConnector.getFileStatus(any())(any(), any()))
          .thenReturn(ResultT.fromValue(UnexpectedError))

        val userAnswers = emptyUserAnswers.withPage(UploadIdPage, testUploadId)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileStatusRoute)
          val result  = route(application, request).value

          status(result)        mustEqual OK
          contentAsJson(result) mustEqual Json.toJson(
            URL(controllers.routes.JourneyRecoveryController.onPageLoad().url)
          )

          verify(mockSubmissionDetailsConnector, times(1)).getFileStatus(eqTo(testUploadId))(any(), any())
        }
      }

      "must return InternalServerError when SubmissionDetailsConnector returns an error" in {
        when(mockSubmissionDetailsConnector.getFileStatus(any())(any(), any()))
          .thenReturn(ResultT.fromError(InternalServerError))

        val userAnswers = emptyUserAnswers.withPage(UploadIdPage, testUploadId)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileStatusRoute)
          val result  = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR

          verify(mockSubmissionDetailsConnector, times(1)).getFileStatus(eqTo(testUploadId))(any(), any())
        }
      }

      "must return InternalServerError when UploadId is missing from user answers" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileStatusRoute)
          val result  = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR

          verify(mockSubmissionDetailsConnector, times(0)).getFileStatus(any())(any(), any())
        }
      }

      "must redirect to Journey Recovery when user answers do not exist" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileStatusRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
