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
import models.errors.ApiError.InternalServerError
import models.fileSubmission.FileStatus
import models.fileSubmission.FileStatus.{Passed, VirusFound}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.XmlFileDetailsStubService
import types.ResultT
import views.html.problem.VirusFoundView

class VirusFoundControllerSpec extends SpecBase {

  private val mockStubService: XmlFileDetailsStubService = mock[XmlFileDetailsStubService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStubService)
  }

  "VirusFoundController" - {

    "must return OK when file status is VirusFound" in {
      when(mockStubService.getFileStatus(any[String]()))
        .thenReturn(ResultT.fromValue[FileStatus](VirusFound))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[XmlFileDetailsStubService].toInstance(mockStubService))
          .build()

      running(application) {
        val request   = FakeRequest(GET, routes.VirusFoundController.onPageLoad().url)
        val result    = route(application, request).value
        val view      = application.injector.instanceOf[VirusFoundView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(appConfig.managementUrl)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery when file status is not VirusFound" in {
      when(mockStubService.getFileStatus(any[String]()))
        .thenReturn(ResultT.fromValue[FileStatus](Passed))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[XmlFileDetailsStubService].toInstance(mockStubService))
          .build()

      running(application) {
        val result =
          route(application, FakeRequest(GET, routes.VirusFoundController.onPageLoad().url)).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when retrieving file status fails" in {
      when(mockStubService.getFileStatus(any[String]()))
        .thenReturn(ResultT.fromError[FileStatus](InternalServerError))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[XmlFileDetailsStubService].toInstance(mockStubService))
          .build()

      running(application) {
        val result =
          route(application, FakeRequest(GET, routes.VirusFoundController.onPageLoad().url)).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when user answers do not exist" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.VirusFoundController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
