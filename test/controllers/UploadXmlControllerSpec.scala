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
import forms.UploadXmlFormProvider
import models.upscan.{Reference, UpscanInitiateResponse}
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.upload.UploadXmlView

class UploadXmlControllerSpec extends SpecBase {

  val formProvider       = new UploadXmlFormProvider()
  val form: Form[String] = formProvider()

  // TODO: Remove when implementing Upscan functionality (CARF-578, CARF-579)
  val upscanInitiateResponse = UpscanInitiateResponse(
    fileReference = Reference("abc"),
    postTarget = "http://localhost:17004/send-a-cryptoasset-report/report/upload-file",
    formFields = Map.empty
  )

  "Upload Xml Controller" - {
    ".onPageLoad" - {
      "must return OK and the correct view for a GET" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, upload.routes.UploadXmlController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[UploadXmlView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(form, upscanInitiateResponse)(request, messages(application)).toString
        }
      }
    }
    ".showError" - {
      "must show returned error when file size is more than 250mb - Upscan Error" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, upload.routes.UploadXmlController.showError("EntityTooLarge", "", "").url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[UploadXmlView]

          status(result)     mustEqual OK
          contentAsString(result) must include("The selected file must be 250MB or less")
        }
      }
      "must show returned error when file not selected - Upscan Error" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(GET, upload.routes.UploadXmlController.showError("octetstream", "rejected", "").url)
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("Select a file")
      }

      "must show returned error when file is virus infected" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(GET, upload.routes.UploadXmlController.showError("VirusFile", "", "").url)
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("The selected file contains a virus")
      }

      "must show returned error when file name length is more than 100 char" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(
          GET,
          upload.routes.UploadXmlController.showError("InvalidArgument", "InvalidFileNameLength", "").url
        )
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include(
          "File name must be 100 characters or less and match the MessageRefId in the file"
        )
      }

      "must show returned error when file name includes a disallowed character" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(
          GET,
          upload.routes.UploadXmlController.showError("InvalidArgument", "disallowedcharacters", "").url
        )
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include(
          "File name can only include letters a to z, numbers 0 to 9, underscore (_), hyphens and full stops"
        )
      }

      "must show returned error when file size is zero kb - JS enabled flow" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request =
          FakeRequest(GET, upload.routes.UploadXmlController.showError("InvalidArgument", "FileIsEmpty", "").url)
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("The selected file is empty")
      }

      "must show returned error when file type mismatch" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request =
          FakeRequest(GET, upload.routes.UploadXmlController.showError("InvalidArgument", "typeMismatch", "").url)
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("The selected file must be an XML")
      }

      "must show returned error when file had invalid argument" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(GET, upload.routes.UploadXmlController.showError("InvalidArgument", "", "").url)
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("Select a file")
      }

      "must show returned error when Unknown error" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(GET, upload.routes.UploadXmlController.showError("UnknownError", "", "").url)
        val result  = route(application, request).value

        status(result)     mustEqual OK
        contentAsString(result) must include("The selected file could not be uploaded")
      }
    }
  }
}
