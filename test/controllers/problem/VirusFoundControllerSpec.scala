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
import org.mockito.Mockito.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import types.ResultT
import views.html.problem.VirusFoundView

class VirusFoundControllerSpec extends SpecBase {

  private val mockSubmissionDetailsConnector: SubmissionDetailsConnector = mock[SubmissionDetailsConnector]

  lazy val virusFoundRoute: String = routes.VirusFoundController.onPageLoad(testUploadId.value).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSubmissionDetailsConnector)
  }

  "VirusFoundController" - {

    "must return OK when file status is VirusFound" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any())(any(), any()))
        .thenReturn(ResultT.fromValue(submissionDetailsVirus))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

      running(application) {
        val request   = FakeRequest(GET, virusFoundRoute)
        val result    = route(application, request).value
        val view      = application.injector.instanceOf[VirusFoundView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(appConfig.managementUrl)(request, messages(application)).toString

        verify(mockSubmissionDetailsConnector).getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any())
      }
    }

    "must redirect to Journey Recovery when file status is not VirusFound" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any())(any(), any()))
        .thenReturn(ResultT.fromValue(orgSubmissionDetailsPassed))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

      running(application) {
        val request = FakeRequest(GET, virusFoundRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockSubmissionDetailsConnector).getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any())
      }
    }

    "must redirect to Journey Recovery when retrieving file status fails" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any())(any(), any()))
        .thenReturn(ResultT.fromError(InternalServerError))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

      running(application) {
        val request = FakeRequest(GET, virusFoundRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockSubmissionDetailsConnector).getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any())
      }
    }
  }
}
