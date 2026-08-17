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
import models.errors.ApiError.InternalServerError
import models.fileSubmission.FileStatus
import models.fileSubmission.FileStatus.{Failed, Passed}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito._
import pages.ExtractedFileDetailsPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.StubService
import types.ResultT
import utils.FileCheckResultHelper
import views.html.upload.FileFailedChecksView

class FileFailedChecksControllerSpec extends SpecBase {

  private val mockStubService: StubService = mock[StubService]

  private val mockFileCheckResultHelper: FileCheckResultHelper = mock[FileCheckResultHelper]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStubService, mockFileCheckResultHelper)
  }

  "FileFailedChecksController" - {

    "must return OK and render the view when file status is Failed and ExtractedFileDetailsPage exists" in {
      when(mockStubService.getFileStatus(any[String]()))
        .thenReturn(ResultT.fromValue[FileStatus](Failed))

      when(
        mockFileCheckResultHelper.summaryList(
          eqTo(testMessageRefId),
          eqTo(Failed),
          eqTo("fileFailedChecks")
        )(any)
      ).thenReturn(testSummaryList)

      val userAnswers =
        emptyUserAnswers.withPage(
          ExtractedFileDetailsPage,
          extractedFileDetailsNilReport
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[StubService].toInstance(mockStubService),
            bind[FileCheckResultHelper].toInstance(mockFileCheckResultHelper)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[FileFailedChecksView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(testSummaryList)(request, messages(application)).toString

        verify(mockFileCheckResultHelper).summaryList(
          eqTo(testMessageRefId),
          eqTo(Failed),
          eqTo("fileFailedChecks")
        )(any)
      }
    }

    "must redirect to Journey Recovery when file status is not Failed" in {
      when(mockStubService.getFileStatus(any[String]()))
        .thenReturn(ResultT.fromValue[FileStatus](Passed))

      val userAnswers =
        emptyUserAnswers.withPage(
          ExtractedFileDetailsPage,
          extractedFileDetailsNilReport
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[StubService].toInstance(mockStubService),
            bind[FileCheckResultHelper].toInstance(mockFileCheckResultHelper)
          )
          .build()

      running(application) {
        val result =
          route(
            application,
            FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
          ).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verifyNoInteractions(mockFileCheckResultHelper)
      }
    }

    "must redirect to Journey Recovery when ExtractedFileDetailsPage is missing" in {
      when(mockStubService.getFileStatus(any[String]()))
        .thenReturn(ResultT.fromValue[FileStatus](Failed))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StubService].toInstance(mockStubService),
            bind[FileCheckResultHelper].toInstance(mockFileCheckResultHelper)
          )
          .build()

      running(application) {
        val result =
          route(
            application,
            FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
          ).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verifyNoInteractions(mockFileCheckResultHelper)
      }
    }

    "must redirect to Journey Recovery when retrieving file status fails" in {
      when(mockStubService.getFileStatus(any[String]()))
        .thenReturn(ResultT.fromError[FileStatus](InternalServerError))

      val userAnswers =
        emptyUserAnswers.withPage(
          ExtractedFileDetailsPage,
          extractedFileDetailsNilReport
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[StubService].toInstance(mockStubService),
            bind[FileCheckResultHelper].toInstance(mockFileCheckResultHelper)
          )
          .build()

      running(application) {
        val result =
          route(
            application,
            FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
          ).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verifyNoInteractions(mockFileCheckResultHelper)
      }
    }

    "must redirect to Journey Recovery when user answers do not exist" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
