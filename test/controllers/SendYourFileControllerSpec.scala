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
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{ExtractedFileDetailsPage, RcaspDetailsPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.SendYourFileView

class SendYourFileControllerSpec extends SpecBase {

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  lazy val sendYourFileRoute: String       = routes.SendYourFileController.onPageLoad().url
  lazy val sendYourFileStatusRoute: String = routes.SendYourFileController.getFileStatusAndRedirect().url

  "SendYourFile Controller" - {

    ".onPageLoad" - {
      when(mockAppConfig.spinnerMaxPollingAttempts) thenReturn 10
      when(mockAppConfig.feedbackUrl(any())) thenReturn "feedbackUrl"

      "must return OK and the correct view with warning text" in {
        val userAnswers = emptyUserAnswers
          .withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)
          .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(
            Some("We cannot complete all checks on test data or accept the file."),
            maxPollingAttempts = 10
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view without warning text" in {
        val userAnswers = emptyUserAnswers
          .withPage(ExtractedFileDetailsPage, extractedFileDetailsNilReport)
          .withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[SendYourFileView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(None, maxPollingAttempts = 10)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery when RcaspDetails is missing from user answers" in {
        val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetailsNilReport)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when ExtractedFileDetails is missing from user answers" in {
        val userAnswers = emptyUserAnswers.withPage(RcaspDetailsPage, organisationRegisteredBusinessRcaspDetails)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when RcaspDetails and ExtractedFileDetails are missing from user answers" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when user answers do not exist" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    ".onSubmit" - {
      // TODO: Update when call to FTS is implemented (CARF-611) and StillCheckingYourFile is created (CARF-616)
      "must submit to FTS and redirect to StillCheckingYourFileController" in {
        val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.PlaceholderController
            .onPageLoad("Should submit to FTS (CARF-611), then redirect to /still-checking-your-file (CARF-616)")
            .url
        }
      }

      "must redirect to Journey Recovery when ExtractedFileDetails is missing from user answers" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when user answers do not exist" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, sendYourFileRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    ".getFileStatusAndRedirect" - {
      // TODO: Update when call to get file status is implemented (CARF-621)
      "must redirect based on the file status" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileStatusRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.PlaceholderController
            .onPageLoad("Redirect to next page based on file status (CARF-621)")
            .url
        }
      }

      "must redirect to Journey Recovery when user answers do not exist" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, sendYourFileStatusRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
