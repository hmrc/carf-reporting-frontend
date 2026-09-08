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

package controllers.submission

import base.SpecBase
import config.FrontendAppConfig
import models.errors.ApiError.InternalServerError
import models.fileSubmission.{ResultOfAutomaticChecksStubData, SlicedCachedFileDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.AutomaticChecksStubService
import types.ResultT
import views.html.submission.ResultOfAutomaticChecksView

class ResultOfAutomaticChecksControllerSpec extends SpecBase {

  private val mockService: AutomaticChecksStubService = mock[AutomaticChecksStubService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockService)
  }

  "ResultOfAutomaticChecks Controller" - {

    "must return OK and the correct view when submissions are found" in {

      when(mockService.getSubmissions(any()))
        .thenReturn(ResultT.fromValue(ResultOfAutomaticChecksStubData.allStatuses))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AutomaticChecksStubService].toInstance(mockService))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, routes.ResultOfAutomaticChecksController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[ResultOfAutomaticChecksView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(ResultOfAutomaticChecksStubData.allStatuses, appConfig.managementUrl)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to Journey Recovery when the submissions list is empty" in {

      when(mockService.getSubmissions(any()))
        .thenReturn(ResultT.fromValue(Seq.empty[SlicedCachedFileDetails]))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AutomaticChecksStubService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.ResultOfAutomaticChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when the service returns an error" in {

      when(mockService.getSubmissions(any()))
        .thenReturn(ResultT.fromError[Seq[SlicedCachedFileDetails]](InternalServerError))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AutomaticChecksStubService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.ResultOfAutomaticChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
