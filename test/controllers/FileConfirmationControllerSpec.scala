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
import models.fileSubmission.FileStatus.Pending
import models.responses.getEmails
import org.mockito.ArgumentMatchers.{any, argThat, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import pages.UploadCompletionLockPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.XmlFileDetailsStubService
import utils.{DateTimeFormats, FileConfirmationHelper}
import views.html.FileConfirmationView

import java.time.LocalDateTime
import scala.concurrent.Future
class FileConfirmationControllerSpec extends SpecBase {

  private val mockAppConfig: FrontendAppConfig                   = mock[FrontendAppConfig]
  private val mockFileConfirmationHelper: FileConfirmationHelper = mock[FileConfirmationHelper]
  private val mockStubService: XmlFileDetailsStubService         = mock[XmlFileDetailsStubService]

  val fileConfirmationRoute: String = controllers.routes.FileConfirmationController
    .onPageLoad(testRcaspId, testUploadId.value)
    .url

  private inline val managementUrl = "http://localhost/management-url"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFileConfirmationHelper)
  }
  "FileConfirmation Controller" - {
    when(mockAppConfig.feedbackUrl(any())) thenReturn "feedbackUrl"
    ".onPageLoad" - {
      "must return OK and the correct view when user answers are complete 2 emails" in {
        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(testSummaryList.rows)
        when(mockStubService.getCachedFileDetails(any(), eqTo(testRcaspId), eqTo(testUploadId.value)))
          .thenReturn(orgFileDetails)
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val formattedDateTime = DateTimeFormats.dateTimeToString(orgFileDetails.dateTime.get)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[XmlFileDetailsStubService].toInstance(mockStubService))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[FileConfirmationView]

          val expectedEmailHtml =
            s"We have sent a confirmation email to ${subscriptionDetailsOrganisation.getEmails.head} " +
              s"and ${subscriptionDetailsOrganisation.getEmails(1)}."

          status(result)          mustEqual OK
          contentAsString(result)      must include(expectedEmailHtml)
          contentAsString(result) mustEqual view(
            testSummaryList,
            formattedDateTime,
            managementUrl,
            expectedEmailHtml
          )(request, messages(application)).toString

          verify(mockSessionRepository, times(1)).set(argThat(_.get(UploadCompletionLockPage).contains(true)))
        }
      }
      "must return OK and the correct view when user answers are complete 1 email" in {

        val orgFileDetailsOneEmail =
          orgFileDetails.copy(subscriptionDetails = subscriptionDetailsOrganisation.copy(secondaryUserDetails = None))

        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(testSummaryList.rows)
        when(mockStubService.getCachedFileDetails(any(), eqTo(testRcaspId), eqTo(testUploadId.value)))
          .thenReturn(orgFileDetailsOneEmail)
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val formattedDateTime = DateTimeFormats.dateTimeToString(orgFileDetailsOneEmail.dateTime.get)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[XmlFileDetailsStubService].toInstance(mockStubService))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[FileConfirmationView]

          val expectedEmailHtml =
            s"We have sent a confirmation email to ${subscriptionDetailsOrganisation.getEmails.head}."

          status(result)          mustEqual OK
          contentAsString(result)      must include(expectedEmailHtml)
          contentAsString(result) mustEqual view(
            testSummaryList,
            formattedDateTime,
            managementUrl,
            expectedEmailHtml
          )(request, messages(application)).toString

          verify(mockSessionRepository, times(1)).set(argThat(_.get(UploadCompletionLockPage).contains(true)))
        }
      }

      "must return OK and the correct view when user answers are complete 4 emails" in {

        val orgFileDetailsOneEmail = orgFileDetails
          .copy(
            rcaspDetails = organisationStandardRcaspDetails,
            subscriptionDetails = subscriptionDetailsOrganisation
          )

        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(testSummaryList.rows)
        when(mockStubService.getCachedFileDetails(any(), eqTo(testRcaspId), eqTo(testUploadId.value)))
          .thenReturn(orgFileDetailsOneEmail)
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val formattedDateTime = DateTimeFormats.dateTimeToString(orgFileDetailsOneEmail.dateTime.get)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[XmlFileDetailsStubService].toInstance(mockStubService))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[FileConfirmationView]

          val expectedEmailHtml =
            s"We have sent a confirmation email to ${subscriptionDetailsOrganisation.getEmails.head}, " +
              s"${subscriptionDetailsOrganisation.getEmails(1)}, " +
              s"${organisationStandardRcaspDetails.getEmails.head} and ${organisationStandardRcaspDetails.getEmails(1)}."

          status(result)          mustEqual OK
          contentAsString(result)      must include(expectedEmailHtml)
          contentAsString(result) mustEqual view(
            testSummaryList,
            formattedDateTime,
            managementUrl,
            expectedEmailHtml
          )(request, messages(application)).toString

          verify(mockSessionRepository, times(1)).set(argThat(_.get(UploadCompletionLockPage).contains(true)))
        }
      }

      "must redirect to Journey Recovery when datetime is missing from cached File Details" in {
        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockStubService.getCachedFileDetails(any(), eqTo(testRcaspId), eqTo(testUploadId.value)))
          .thenReturn(orgFileDetails.copy(dateTime = None))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[XmlFileDetailsStubService].toInstance(mockStubService))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when file status is not Passed from cached File Details" in {
        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockStubService.getCachedFileDetails(any(), eqTo(testRcaspId), eqTo(testUploadId.value)))
          .thenReturn(orgFileDetails.copy(fileStatus = Pending))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[XmlFileDetailsStubService].toInstance(mockStubService))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when getCachedFileDetails returns no extracted file details" in {

        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockStubService.getCachedFileDetails(any(), eqTo(testRcaspId), eqTo(testUploadId.value)))
          .thenReturn(orgFileDetails.copy(extractedFileDetails = None))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[XmlFileDetailsStubService].toInstance(mockStubService))
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
