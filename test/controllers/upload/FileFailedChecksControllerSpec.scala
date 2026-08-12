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

package controllers.upload

import base.SpecBase
import models.filecheck.FileCheckStatus.{Failed, Passed, Virus}
import models.filecheck.FileCheckResult
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.StubService
import views.html.upload.FileFailedChecksView

class FileFailedChecksControllerSpec extends SpecBase {

  private val mockStubService: StubService = mock[StubService]

  private val messageRefId =
    "GB2026GB-CARF01234567890-Cryptoasset-Reporting-Framework-XML-Report_for_2026_My-Company-Limited_0001"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStubService)
  }

  "FileFailedChecksController" - {

    "must return OK and render the view when the result status is Failed" in {

      when(mockStubService.getFileCheckResult(any[String]()))
        .thenReturn(Some(FileCheckResult(Failed, messageRefId)))

      val application =
        applicationBuilder()
          .overrides(bind[StubService].toInstance(mockStubService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[FileFailedChecksView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(messageRefId)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery when the result status is Passed" in {

      when(mockStubService.getFileCheckResult(any[String]()))
        .thenReturn(Some(FileCheckResult(Passed, messageRefId)))

      val application =
        applicationBuilder()
          .overrides(bind[StubService].toInstance(mockStubService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when the result status is Virus" in {

      when(mockStubService.getFileCheckResult(any[String]()))
        .thenReturn(Some(FileCheckResult(Virus, messageRefId)))

      val application =
        applicationBuilder()
          .overrides(bind[StubService].toInstance(mockStubService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.FilePassedChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when no file-check result is found" in {

      when(mockStubService.getFileCheckResult(any[String]()))
        .thenReturn(None)

      val application =
        applicationBuilder()
          .overrides(bind[StubService].toInstance(mockStubService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
