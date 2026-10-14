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
import connectors.SubmissionDetailsConnector
import models.errors.ApiError.InternalServerError
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import types.ResultT
import views.html.problem.FileNotAcceptedView

class FileNotAcceptedControllerSpec extends SpecBase {

  val mockAppConfig: FrontendAppConfig                           = mock[FrontendAppConfig]
  val mockSubmissionDetailsConnector: SubmissionDetailsConnector = mock[SubmissionDetailsConnector]

  val testEmailAddress: String = "aeoi.enquiries@hmrc.gov.uk"

  lazy val fileNotAcceptedRoute: String = routes.FileNotAcceptedController.onPageLoad(testUploadId.value).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAppConfig, mockSubmissionDetailsConnector)
  }

  "FileNotAccepted Controller" - {

    ".onPageLoad" - {

      "must return OK and the correct view when file status is UnprocessableErrorFile" in {
        when(mockAppConfig.aeoiEmailAddress) thenReturn testEmailAddress
        when(mockAppConfig.feedbackUrl(any())) thenReturn "feedbackUrl"
        when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any())(any(), any()))
          .thenReturn(ResultT.fromValue(submissionDetailsUnprocessableErrorFile))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[FrontendAppConfig].toInstance(mockAppConfig),
            bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, fileNotAcceptedRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[FileNotAcceptedView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(testEmailAddress)(request, messages(application)).toString

          verify(mockSubmissionDetailsConnector, times(1))
            .getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any())
        }
      }

      "must redirect to Journey Recovery when file status is not UnprocessableErrorFile" in {
        when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any())(any(), any()))
          .thenReturn(ResultT.fromValue(orgSubmissionDetailsPassed))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[FrontendAppConfig].toInstance(mockAppConfig),
            bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, fileNotAcceptedRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when SubmissionDetailsConnector returns an error" in {
        when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any())(any(), any()))
          .thenReturn(ResultT.fromError(InternalServerError))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[FrontendAppConfig].toInstance(mockAppConfig),
            bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, fileNotAcceptedRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
