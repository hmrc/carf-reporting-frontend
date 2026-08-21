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
import models.responses.getEmails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.RcaspDetailsPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.{DateTimeFormats, FileConfirmationHelper}
import views.html.FileConfirmationView

import java.time.{Clock, LocalDateTime}

import viewmodels.govuk.summarylist.*
class FileConfirmationControllerSpec extends SpecBase {

  val mockAppConfig: FrontendAppConfig                   = mock[FrontendAppConfig]
  val mockFileConfirmationHelper: FileConfirmationHelper = mock[FileConfirmationHelper]

  val fileConfirmationRoute: String = controllers.routes.FileConfirmationController.onPageLoad().url

  private val formattedDateTime    = DateTimeFormats.dateTimeToString(LocalDateTime.now(clock))
  private inline val managementUrl = "http://localhost/management-url"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFileConfirmationHelper)
  }
  "FileConfirmation Controller" - {
    when(mockAppConfig.feedbackUrl(any())) thenReturn "feedbackUrl"
    ".onPageLoad" - {
      "must return OK and the correct view when user answers are complete" in {
        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(Some(testSummaryList.rows))

        val userAnswers = emptyUserAnswers
          .withPage(RcaspDetailsPage, organisationStandardRcaspDetails)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[Clock].toInstance(clock))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[FileConfirmationView]

          val expectedEmailHtml =
            s"We have sent a confirmation email to ${organisationStandardRcaspDetails.getEmails.head} " +
              s"and ${organisationStandardRcaspDetails.getEmails(1)}."

          status(result)          mustEqual OK
          contentAsString(result)      must include(expectedEmailHtml)
          contentAsString(result) mustEqual view(
            testSummaryList.withCssClass("govuk-!-margin-bottom-5"),
            formattedDateTime,
            false,
            managementUrl,
            expectedEmailHtml
          )(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery when RcaspDetails is missing from user answers" in {
        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(Some(testSummaryList.rows))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when FileConfirmationHelper returns None" in {

        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(None)

        val userAnswers = emptyUserAnswers.withPage(RcaspDetailsPage, organisationStandardRcaspDetails)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when user answers do not exist at all" in {

        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(Some(testSummaryList.rows))

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
