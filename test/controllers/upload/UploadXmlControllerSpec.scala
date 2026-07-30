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
import config.FrontendAppConfig
import connectors.UpscanConnector
import controllers.upload
import forms.UploadXmlFormProvider
import models.errors.ApiError.InternalServerError
import models.upscan.*
import models.upscan.UploadStatus.*
import org.mockito.ArgumentMatchers.{any, argThat, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import pages.{FileReferencePage, UploadIdPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import types.ResultT
import views.html.upload.UploadXmlView

import scala.concurrent.Future

class UploadXmlControllerSpec extends SpecBase {

  val mockUpscanConnector: UpscanConnector = mock[UpscanConnector]
  val mockAppConfig: FrontendAppConfig     = mock[FrontendAppConfig]

  val formProvider       = new UploadXmlFormProvider()
  val form: Form[String] = formProvider()

  lazy val onPageLoadRoute: String = controllers.upload.routes.UploadXmlController.onPageLoad().url
  lazy val getStatusRoute: String  = controllers.upload.routes.UploadXmlController.getStatus(testUploadId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUpscanConnector, mockAppConfig)
  }

  "UploadXml Controller" - {
    ".onPageLoad" - {
      "must return OK and the correct view for a GET" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any())).thenReturn(ResultT.fromValue((): Unit))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute).withCSRFToken

          val result = route(application, request).value

          val view = application.injector.instanceOf[UploadXmlView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(form, upscanInitiateResponse)(request, messages(application)).toString

          verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
          verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
          verify(mockSessionRepository, times(1)).set(
            argThat(ua => ua.get(UploadIdPage).nonEmpty && ua.get(FileReferencePage).nonEmpty)
          )
        }
      }

      "must redirect to journey recovery when UpscanConnector .getUpscanFormData returns an error" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromError(InternalServerError))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute).withCSRFToken

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
          verify(mockUpscanConnector, times(0)).requestUpload(any(), eqTo(testReference))(any(), any())
          verify(mockSessionRepository, times(0)).set(any())
        }
      }

      "must redirect to journey recovery when UpscanConnector .requestUpload returns an error" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any()))
          .thenReturn(ResultT.fromError(InternalServerError))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute).withCSRFToken

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
          verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
          verify(mockSessionRepository, times(0)).set(any())
        }
      }
    }

    ".showError" - {
      "must show returned error when file size is more than 250mb - Upscan Error" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any())).thenReturn(ResultT.fromValue((): Unit))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        running(application) {
          val request =
            FakeRequest(GET, upload.routes.UploadXmlController.showError("EntityTooLarge", "", "").url).withCSRFToken
          val result  = route(application, request).value

          status(result)     mustEqual OK
          contentAsString(result) must include("The selected file must be 250MB or less")

          verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
          verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
          verify(mockSessionRepository, times(1)).set(
            argThat(ua => ua.get(UploadIdPage).nonEmpty && ua.get(FileReferencePage).nonEmpty)
          )
        }
      }

      "must show returned error when file not selected - Upscan Error" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any())).thenReturn(ResultT.fromValue((): Unit))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        val request =
          FakeRequest(GET, upload.routes.UploadXmlController.showError("octetstream", "rejected", "").url).withCSRFToken
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("Select a file")

        verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
        verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
        verify(mockSessionRepository, times(1)).set(
          argThat(ua => ua.get(UploadIdPage).nonEmpty && ua.get(FileReferencePage).nonEmpty)
        )
      }

      "must show returned error when file is virus infected" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any())).thenReturn(ResultT.fromValue((): Unit))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        val request =
          FakeRequest(GET, upload.routes.UploadXmlController.showError("VirusFile", "", "").url).withCSRFToken
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("The selected file contains a virus")

        verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
        verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
        verify(mockSessionRepository, times(1)).set(
          argThat(ua => ua.get(UploadIdPage).nonEmpty && ua.get(FileReferencePage).nonEmpty)
        )
      }

      "must show returned error when file name length is more than 100 char" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any())).thenReturn(ResultT.fromValue((): Unit))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        val request = FakeRequest(
          GET,
          upload.routes.UploadXmlController.showError("InvalidArgument", "InvalidFileNameLength", "").url
        ).withCSRFToken
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include(
          "File name must be 100 characters or less and match the MessageRefId in the file"
        )

        verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
        verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
        verify(mockSessionRepository, times(1)).set(
          argThat(ua => ua.get(UploadIdPage).nonEmpty && ua.get(FileReferencePage).nonEmpty)
        )
      }

      "must show returned error when file name includes a disallowed character" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any())).thenReturn(ResultT.fromValue((): Unit))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        val request = FakeRequest(
          GET,
          upload.routes.UploadXmlController.showError("InvalidArgument", "disallowedcharacters", "").url
        ).withCSRFToken
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include(
          "File name can only include letters a to z, numbers 0 to 9, underscore (_), hyphens and full stops"
        )

        verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
        verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
        verify(mockSessionRepository, times(1)).set(
          argThat(ua => ua.get(UploadIdPage).nonEmpty && ua.get(FileReferencePage).nonEmpty)
        )
      }

      "must show returned error when file size is zero kb - JS enabled flow" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any())).thenReturn(ResultT.fromValue((): Unit))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        val request =
          FakeRequest(
            GET,
            upload.routes.UploadXmlController.showError("InvalidArgument", "FileIsEmpty", "").url
          ).withCSRFToken
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("The selected file is empty")

        verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
        verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
        verify(mockSessionRepository, times(1)).set(
          argThat(ua => ua.get(UploadIdPage).nonEmpty && ua.get(FileReferencePage).nonEmpty)
        )
      }

      "must show returned error when file type mismatch" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any())).thenReturn(ResultT.fromValue((): Unit))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        val request =
          FakeRequest(
            GET,
            upload.routes.UploadXmlController.showError("InvalidArgument", "typeMismatch", "").url
          ).withCSRFToken
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("The selected file must be an XML")

        verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
        verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
        verify(mockSessionRepository, times(1)).set(
          argThat(ua => ua.get(UploadIdPage).nonEmpty && ua.get(FileReferencePage).nonEmpty)
        )
      }

      "must show returned error when file had invalid argument" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any())).thenReturn(ResultT.fromValue((): Unit))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        val request =
          FakeRequest(GET, upload.routes.UploadXmlController.showError("InvalidArgument", "", "").url).withCSRFToken
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("Select a file")

        verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
        verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
        verify(mockSessionRepository, times(1)).set(
          argThat(ua => ua.get(UploadIdPage).nonEmpty && ua.get(FileReferencePage).nonEmpty)
        )
      }

      "must show returned error when Unknown error" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any())).thenReturn(ResultT.fromValue((): Unit))

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        val request =
          FakeRequest(GET, upload.routes.UploadXmlController.showError("UnknownError", "", "").url).withCSRFToken
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("The selected file could not be uploaded")

        verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
        verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
        verify(mockSessionRepository, times(1)).set(
          argThat(ua => ua.get(UploadIdPage).nonEmpty && ua.get(FileReferencePage).nonEmpty)
        )
      }

      "must redirect to journey recovery when UpscanConnector .getUpscanFormData returns an error" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromError(InternalServerError))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        running(application) {
          val request =
            FakeRequest(GET, upload.routes.UploadXmlController.showError("VirusFile", "", "").url).withCSRFToken

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
          verify(mockUpscanConnector, times(0)).requestUpload(any(), eqTo(testReference))(any(), any())
          verify(mockSessionRepository, times(0)).set(any())
        }
      }

      "must redirect to journey recovery when UpscanConnector .requestUpload returns an error" in {
        when(mockUpscanConnector.getUpscanFormData(any())(any(), any()))
          .thenReturn(ResultT.fromValue(upscanInitiateResponse))

        when(mockUpscanConnector.requestUpload(any(), any())(any(), any()))
          .thenReturn(ResultT.fromError(InternalServerError))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        running(application) {
          val request =
            FakeRequest(GET, upload.routes.UploadXmlController.showError("VirusFile", "", "").url).withCSRFToken

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockUpscanConnector, times(1)).getUpscanFormData(any())(any(), any())
          verify(mockUpscanConnector, times(1)).requestUpload(any(), eqTo(testReference))(any(), any())
          verify(mockSessionRepository, times(0)).set(any())
        }
      }
    }

    ".getStatus" - {
      "must read the progress of the upload from the backend and redirect accordingly" in {
        def verifyResult(uploadStatus: UploadStatus, expectedRedirectUrl: String): Unit = {
          reset(mockUpscanConnector)
          when(mockUpscanConnector.getUploadStatus(any())(any(), any()))
            .thenReturn(ResultT.fromValue(Some(uploadStatus)))

          when(mockAppConfig.upscanCallbackDelayInSeconds).thenReturn(0)
          when(mockAppConfig.upscanMaxFileNameLength).thenReturn(100)

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[UpscanConnector].toInstance(mockUpscanConnector),
              bind[FrontendAppConfig].toInstance(mockAppConfig)
            )
            .build()

          running(application) {
            val request = FakeRequest(GET, getStatusRoute)
            val result  = route(application, request).value

            status(result)                 mustBe SEE_OTHER
            redirectLocation(result).value mustBe expectedRedirectUrl

            verify(mockUpscanConnector, times(1)).getUploadStatus(eqTo(testUploadId))(any(), any())
          }
        }

        val invalidFileName = stringsLongerThan(101).sample.get.concat(".xml")

        verifyResult(InProgress, upload.routes.UploadXmlController.getStatus(testUploadId).url)
        verifyResult(Quarantined, upload.routes.UploadXmlController.showError("virusfile", "", "").url)
        verifyResult(
          uploadRejected,
          upload.routes.UploadXmlController.showError("invalidargument", "typemismatch", "").url
        )
        verifyResult(
          UploadRejected(ErrorDetails("REJECTED", "octet-stream")),
          upload.routes.UploadXmlController.showError("octetstream", "rejected", "").url
        )
        verifyResult(Failed, upload.routes.UploadXmlController.showError("UploadFailed", "", "").url)
        verifyResult(
          uploadedSuccessfully,
          controllers.routes.PlaceholderController
            .onPageLoad("Upscan checks passed. Should redirect to FileValidationController (CARF-596)")
            .url
        )
        verifyResult(
          uploadedSuccessfully.copy(name = invalidFileName),
          upload.routes.UploadXmlController.showError("invalidargument", "invalidfilenamelength", "").url
        )
        verifyResult(
          uploadedSuccessfully.copy(name = "disallowed???<>!!!.xml"),
          upload.routes.UploadXmlController.showError("invalidargument", "disallowedcharacters", "").url
        )
        verifyResult(
          uploadedSuccessfully.copy(name = "not-xml.png"),
          upload.routes.UploadXmlController.showError("invalidargument", "typemismatch", "").url
        )
        verifyResult(
          uploadedSuccessfully.copy(size = 0L),
          upload.routes.UploadXmlController.showError("invalidargument", "fileisempty", "").url
        )
      }

      "must show error when UpscanConnector .getUploadStatus returns None" in {
        when(mockUpscanConnector.getUploadStatus(any())(any(), any())).thenReturn(ResultT.fromValue(None))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UpscanConnector].toInstance(mockUpscanConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, getStatusRoute)
          val result  = route(application, request).value

          status(result)                 mustBe SEE_OTHER
          redirectLocation(result).value mustBe upload.routes.UploadXmlController.showError("UploadFailed", "", "").url

          verify(mockUpscanConnector, times(1)).getUploadStatus(eqTo(testUploadId))(any(), any())
        }
      }

      "must redirect to journey recovery when UpscanConnector .getUploadStatus returns an error" in {
        when(mockUpscanConnector.getUploadStatus(any())(any(), any()))
          .thenReturn(ResultT.fromError(InternalServerError))

        when(mockAppConfig.upscanCallbackDelayInSeconds).thenReturn(0)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UpscanConnector].toInstance(mockUpscanConnector),
            bind[FrontendAppConfig].toInstance(mockAppConfig)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, getStatusRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockUpscanConnector, times(1)).getUploadStatus(eqTo(testUploadId))(any(), any())
        }
      }

      "must redirect to journey recovery when user answers is missing" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, getStatusRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
