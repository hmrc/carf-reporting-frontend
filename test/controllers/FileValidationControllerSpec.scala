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
import connectors.FileValidationConnector
import models.errors.ApiError.InternalServerError
import models.errors.{InvalidXmlError, XmlErrors}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import pages.{ExtractedFileDetailsPage, UploadSuccessDetailsPage, XmlErrorsPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import types.ResultT

import scala.concurrent.Future

class FileValidationControllerSpec extends SpecBase {

  val mockFileValidationConnector: FileValidationConnector = mock[FileValidationConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFileValidationConnector)
  }

  lazy val fileValidationRoute: String = controllers.upload.routes.FileValidationController.onPageLoad().url

  "FileValidation Controller" - {

    "must redirect to RcaspAndSubscriptionDetailsController when FileValidationConnector returns extracted file details" in {
      val userAnswers = emptyUserAnswers.withPage(UploadSuccessDetailsPage, uploadSuccessDetails)

      when(mockFileValidationConnector.validateUploadedFile(any())(any(), any()))
        .thenReturn(ResultT.fromValue(extractedFileDetailsTestData))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileValidationConnector].toInstance(mockFileValidationConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, fileValidationRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.RcaspAndSubscriptionDetailsController.onPageLoad().url

        verify(mockFileValidationConnector, times(1)).validateUploadedFile(eqTo(testDownloadUrl))(any(), any())
        verify(mockSessionRepository, times(1)).set(
          userAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
        )
      }
    }

    "must redirect to InvalidXmlController when FileValidationConnector returns InvalidXmlError" in {
      val userAnswers = emptyUserAnswers.withPage(UploadSuccessDetailsPage, uploadSuccessDetails)

      when(mockFileValidationConnector.validateUploadedFile(any())(any(), any()))
        .thenReturn(ResultT.fromError(InvalidXmlError))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileValidationConnector].toInstance(mockFileValidationConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, fileValidationRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.InvalidXmlController.onPageLoad().url

        verify(mockFileValidationConnector, times(1)).validateUploadedFile(eqTo(testDownloadUrl))(any(), any())
        verify(mockSessionRepository, times(0)).set(any())
      }
    }

    "must redirect to DataErrorsController when FileValidationConnector returns XML schema errors" in {
      val userAnswers = emptyUserAnswers.withPage(UploadSuccessDetailsPage, uploadSuccessDetails)

      when(mockFileValidationConnector.validateUploadedFile(any())(any(), any()))
        .thenReturn(ResultT.fromError(XmlErrors(xmlFewErrors)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileValidationConnector].toInstance(mockFileValidationConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, fileValidationRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.DataErrorsController.onPageLoad().url

        verify(mockFileValidationConnector, times(1)).validateUploadedFile(eqTo(testDownloadUrl))(any(), any())
        verify(mockSessionRepository, times(1)).set(eqTo(userAnswers.withPage(XmlErrorsPage, xmlFewErrors)))
      }
    }

    "must redirect to Journey Recovery when FileValidationConnector returns InternalServerError" in {
      val userAnswers = emptyUserAnswers.withPage(UploadSuccessDetailsPage, uploadSuccessDetails)

      when(mockFileValidationConnector.validateUploadedFile(any())(any(), any()))
        .thenReturn(ResultT.fromError(InternalServerError))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileValidationConnector].toInstance(mockFileValidationConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, fileValidationRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockFileValidationConnector, times(1)).validateUploadedFile(eqTo(testDownloadUrl))(any(), any())
        verify(mockSessionRepository, times(0)).set(any())
      }
    }

    "must redirect to Journey Recovery when UploadSuccessDetails is missing from user answers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[FileValidationConnector].toInstance(mockFileValidationConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, fileValidationRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockFileValidationConnector, times(0)).validateUploadedFile(any())(any(), any())
        verify(mockSessionRepository, times(0)).set(any())
      }
    }

    "must redirect to Journey Recovery when user answers do not exist" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, fileValidationRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
