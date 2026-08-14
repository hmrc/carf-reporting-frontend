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
import models.{DocTypeIndic, ExtractedFileDetails, MessageTypeIndic}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import pages.ExtractedFileDetailsPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.StubService
import types.ResultT
import views.html.upload.FileFailedChecksView

class FileFailedChecksControllerSpec extends SpecBase {

  private val mockStubService: StubService = mock[StubService]

  private val extractedFileDetails = ExtractedFileDetails(
    messageRefId =
      "GB2026GB-CARF01234567890-Cryptoasset-Reporting-Framework-XML-Report_for_2026_My-Company-Limited_0001",
    sendingEntityIn = "ZMCAR0123456788",
    rcaspName = Some("Timmy's Turtles"),
    messageTypeIndic = MessageTypeIndic.CARF701,
    hasOtherNexus = false,
    hasCryptoUsers = true,
    docTypeIndic = DocTypeIndic.OECD1,
    isTestData = false,
    allCryptoUsersAreCorrections = false,
    allCryptoUsersAreDeletions = false
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStubService)
  }

  "FileFailedChecksController" - {

    "must return OK and render the view when status is Failed and ExtractedFileDetailsPage is present" in {

      when(mockStubService.getFileStatus(any[String]())).thenReturn(ResultT.fromValue[FileStatus](Failed))

      val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetails)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[StubService].toInstance(mockStubService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[FileFailedChecksView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(extractedFileDetails.messageRefId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery when status is Passed" in {

      when(mockStubService.getFileStatus(any[String]())).thenReturn(ResultT.fromValue[FileStatus](Passed))

      val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetails)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[StubService].toInstance(mockStubService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when status is Failed but ExtractedFileDetailsPage is missing" in {

      when(mockStubService.getFileStatus(any[String]())).thenReturn(ResultT.fromValue[FileStatus](Failed))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StubService].toInstance(mockStubService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when there is no UserAnswers at all" in {

      when(mockStubService.getFileStatus(any[String]())).thenReturn(ResultT.fromValue[FileStatus](Failed))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[StubService].toInstance(mockStubService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.FileFailedChecksController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when getFileStatus returns an error" in {

      when(mockStubService.getFileStatus(any[String]())).thenReturn(ResultT.fromError[FileStatus](InternalServerError))

      val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetails)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
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
