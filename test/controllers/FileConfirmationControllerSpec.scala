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
import connectors.SubmissionDetailsConnector
import models.errors.ApiError.InternalServerError
import models.fileSubmission.FileStatus.Pending
import models.fileSubmission.SubmissionDetails
import models.responses.{getEmails, getEmailsFromSubscriptionDetails}
import org.mockito.ArgumentMatchers.{any, argThat, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import pages.UploadCompletionLockPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import types.ResultT
import utils.{DateTimeFormats, FileConfirmationHelper}
import views.html.FileConfirmationView

import java.time.ZoneOffset
import scala.concurrent.Future

class FileConfirmationControllerSpec extends SpecBase {

  private val mockAppConfig: FrontendAppConfig                           = mock[FrontendAppConfig]
  private val mockFileConfirmationHelper: FileConfirmationHelper         = mock[FileConfirmationHelper]
  private val mockSubmissionDetailsConnector: SubmissionDetailsConnector = mock[SubmissionDetailsConnector]

  lazy val fileConfirmationRoute: String = controllers.routes.FileConfirmationController
    .onPageLoad(testUploadId.value)
    .url

  private inline val managementUrl = "http://localhost/management-url"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSubmissionDetailsConnector, mockFileConfirmationHelper)
  }

  private def confirmationEmailText(submissionDetails: SubmissionDetails): (Int, String) = {
    when(mockAppConfig.managementUrl) thenReturn managementUrl
    when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(testSummaryList.rows)
    when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any(), any())(any(), any()))
      .thenReturn(ResultT.fromValue(submissionDetails))
    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
      .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
      .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
      .build()

    running(application) {
      val result = route(application, FakeRequest(GET, fileConfirmationRoute)).value
      (status(result), contentAsString(result))
    }
  }

  "FileConfirmation Controller" - {
    when(mockAppConfig.feedbackUrl(any())) thenReturn "feedbackUrl"
    ".onPageLoad" - {
      "must return OK and the correct view when user answers are complete 2 emails" in {
        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(testSummaryList.rows)
        when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any(), any())(any(), any()))
          .thenReturn(ResultT.fromValue(orgSubmissionDetailsPassed))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val datetime = orgSubmissionDetailsPassed.lastStatusUpdateTime.atZone(ZoneOffset.UTC).toLocalDateTime

        val formattedDateTime = DateTimeFormats.dateTimeToString(datetime)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[FileConfirmationView]

          val expectedEmailHtml =
            s"We have sent a confirmation email to ${displaySubscriptionDetailsOrg.getEmailsFromSubscriptionDetails.head} " +
              s"and ${displaySubscriptionDetailsOrg.getEmailsFromSubscriptionDetails(1)}."

          status(result)          mustEqual OK
          contentAsString(result)      must include(expectedEmailHtml)
          contentAsString(result) mustEqual view(
            testSummaryList,
            formattedDateTime,
            managementUrl,
            expectedEmailHtml,
            false,
            testRcaspName
          )(request, messages(application)).toString

          verify(mockSubmissionDetailsConnector, times(1))
            .getSubmissionDetailsByUploadId(eqTo(testUploadId), eqTo(testCarfId))(any(), any())
          verify(mockFileConfirmationHelper, times(1))
            .rows(eqTo(extractedFileDetailsTestData), eqTo(testRcaspName))(any())
          verify(mockSessionRepository, times(1)).set(argThat(_.get(UploadCompletionLockPage).contains(true)))
        }
      }

      "must return OK and the correct view when user answers are complete 1 email" in {
        val orgFileDetailsOneEmail =
          orgSubmissionDetailsPassed.copy(
            subscriptionDetails = displaySubscriptionDetailsOrg.copy(secondaryContact = None),
            extractedFileDetails = extractedFileDetailsTestData
              .copy(rcaspName = None, hasOtherNexus = true, hasCryptoUsers = false, isTestData = false)
          )

        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(testSummaryList.rows)
        when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any(), any())(any(), any()))
          .thenReturn(ResultT.fromValue(orgFileDetailsOneEmail))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val datetime = orgSubmissionDetailsPassed.lastStatusUpdateTime.atZone(ZoneOffset.UTC).toLocalDateTime

        val formattedDateTime = DateTimeFormats.dateTimeToString(datetime)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[FileConfirmationView]

          val expectedEmailHtml =
            s"We have sent a confirmation email to ${displaySubscriptionDetailsOrg.getEmailsFromSubscriptionDetails.head}."

          val defaultRcaspName = "the RCASP"

          status(result)          mustEqual OK
          contentAsString(result)      must include(expectedEmailHtml)
          contentAsString(result) mustEqual view(
            testSummaryList,
            formattedDateTime,
            managementUrl,
            expectedEmailHtml,
            true,
            defaultRcaspName
          )(request, messages(application)).toString

          verify(mockSubmissionDetailsConnector, times(1))
            .getSubmissionDetailsByUploadId(eqTo(testUploadId), eqTo(testCarfId))(any(), any())
          verify(mockFileConfirmationHelper, times(1))
            .rows(
              eqTo(
                extractedFileDetailsTestData
                  .copy(rcaspName = None, hasOtherNexus = true, hasCryptoUsers = false, isTestData = false)
              ),
              eqTo(testRcaspName)
            )(any())
          verify(mockSessionRepository, times(1)).set(argThat(_.get(UploadCompletionLockPage).contains(true)))
        }
      }

      "must return OK and the correct view when user answers are complete 4 emails" in {
        val orgFileDetailsFourEmails = orgSubmissionDetailsPassed
          .copy(
            rcaspDetails = organisationStandardRcaspDetails,
            subscriptionDetails = displaySubscriptionDetailsOrg
          )

        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(testSummaryList.rows)
        when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any(), any())(any(), any()))
          .thenReturn(ResultT.fromValue(orgFileDetailsFourEmails))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val datetime = orgSubmissionDetailsPassed.lastStatusUpdateTime.atZone(ZoneOffset.UTC).toLocalDateTime

        val formattedDateTime = DateTimeFormats.dateTimeToString(datetime)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[FileConfirmationView]

          val expectedEmailHtml =
            s"We have sent a confirmation email to ${displaySubscriptionDetailsOrg.getEmailsFromSubscriptionDetails.head}, " +
              s"${displaySubscriptionDetailsOrg.getEmailsFromSubscriptionDetails(1)}, " +
              s"${organisationStandardRcaspDetails.getEmails.head} and ${organisationStandardRcaspDetails.getEmails(1)}."

          status(result)          mustEqual OK
          contentAsString(result)      must include(expectedEmailHtml)
          contentAsString(result) mustEqual view(
            testSummaryList,
            formattedDateTime,
            managementUrl,
            expectedEmailHtml,
            false,
            testRcaspName
          )(request, messages(application)).toString

          verify(mockSubmissionDetailsConnector, times(1))
            .getSubmissionDetailsByUploadId(eqTo(testUploadId), eqTo(testCarfId))(any(), any())
          verify(mockFileConfirmationHelper, times(1))
            .rows(eqTo(extractedFileDetailsTestData), eqTo(testRcaspName))(any())
          verify(mockSessionRepository, times(1)).set(argThat(_.get(UploadCompletionLockPage).contains(true)))
        }
      }

      "must return OK and the correct view when user answers do not exist (accessing from results-of-automatic-checks)" in {
        val orgFileDetailsOneEmail =
          orgSubmissionDetailsPassed.copy(subscriptionDetails =
            displaySubscriptionDetailsOrg.copy(secondaryContact = None)
          )

        when(mockAppConfig.managementUrl) thenReturn "http://localhost/management-url"
        when(mockFileConfirmationHelper.rows(any(), any())(any())).thenReturn(testSummaryList.rows)
        when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any(), any())(any(), any()))
          .thenReturn(ResultT.fromValue(orgFileDetailsOneEmail))

        val datetime = orgSubmissionDetailsPassed.lastStatusUpdateTime.atZone(ZoneOffset.UTC).toLocalDateTime

        val formattedDateTime = DateTimeFormats.dateTimeToString(datetime)

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[FileConfirmationView]

          val expectedEmailHtml =
            s"We have sent a confirmation email to ${displaySubscriptionDetailsOrg.getEmailsFromSubscriptionDetails.head}."

          status(result)          mustEqual OK
          contentAsString(result)      must include(expectedEmailHtml)
          contentAsString(result) mustEqual view(
            testSummaryList,
            formattedDateTime,
            managementUrl,
            expectedEmailHtml,
            false,
            testRcaspName
          )(request, messages(application)).toString

          verify(mockSubmissionDetailsConnector, times(1))
            .getSubmissionDetailsByUploadId(eqTo(testUploadId), eqTo(testCarfId))(any(), any())
          verify(mockFileConfirmationHelper, times(1))
            .rows(eqTo(extractedFileDetailsTestData), eqTo(testRcaspName))(any())
          verify(mockSessionRepository, times(0)).set(any())
        }
      }

      "must list the address once when the only RCASP contact has the same email as the only user contact" in {
        val userEmail = displaySubscriptionDetailsOrg.getEmailsFromSubscriptionDetails.head

        val submission = orgSubmissionDetailsPassed.copy(
          subscriptionDetails = displaySubscriptionDetailsOrg.copy(secondaryContact = None),
          rcaspDetails = organisationStandardRcaspDetails.copy(
            IsRCASPUser = false,
            PrimaryContactDetails =
              organisationStandardRcaspDetails.PrimaryContactDetails.copy(EmailAddress = userEmail),
            SecondaryContactDetails = None
          )
        )

        val (statusCode, body) = confirmationEmailText(submission)

        statusCode mustEqual OK
        body            must include(s"We have sent a confirmation email to $userEmail.")
        body            must not include s"$userEmail and "
      }

      "must treat RCASP and user emails as the same when they differ only by case or surrounding spaces" in {
        val userEmail = displaySubscriptionDetailsOrg.getEmailsFromSubscriptionDetails.head

        val submission = orgSubmissionDetailsPassed.copy(
          subscriptionDetails = displaySubscriptionDetailsOrg.copy(secondaryContact = None),
          rcaspDetails = organisationStandardRcaspDetails.copy(
            IsRCASPUser = false,
            PrimaryContactDetails = organisationStandardRcaspDetails.PrimaryContactDetails
              .copy(EmailAddress = s"  ${userEmail.toUpperCase} "),
            SecondaryContactDetails = None
          )
        )

        val (statusCode, body) = confirmationEmailText(submission)

        statusCode mustEqual OK
        body            must include(s"We have sent a confirmation email to $userEmail.")
      }

      "must only add the RCASP contacts whose email is not a user email" in {
        val userEmails = displaySubscriptionDetailsOrg.getEmailsFromSubscriptionDetails
        val otherEmail = organisationStandardRcaspDetails.getEmails(1)

        val submission = orgSubmissionDetailsPassed.copy(
          subscriptionDetails = displaySubscriptionDetailsOrg,
          rcaspDetails = organisationStandardRcaspDetails.copy(
            IsRCASPUser = false,
            PrimaryContactDetails =
              organisationStandardRcaspDetails.PrimaryContactDetails.copy(EmailAddress = userEmails.head)
          )
        )

        val (statusCode, body) = confirmationEmailText(submission)

        statusCode mustEqual OK
        body            must include(
          s"We have sent a confirmation email to ${userEmails.head}, ${userEmails(1)} and $otherEmail."
        )
      }

      "must only list the user emails when the user is the RCASP" in {
        val submission = orgSubmissionDetailsPassed.copy(
          subscriptionDetails = displaySubscriptionDetailsOrg,
          rcaspDetails = organisationStandardRcaspDetails.copy(IsRCASPUser = true)
        )

        val userEmails = displaySubscriptionDetailsOrg.getEmailsFromSubscriptionDetails

        val (statusCode, body) = confirmationEmailText(submission)

        statusCode mustEqual OK
        body            must include(s"We have sent a confirmation email to ${userEmails.head} and ${userEmails(1)}.")
        body            must not include testRcaspEmail
        body            must not include organisationStandardRcaspDetails.getEmails(1)
      }

      "must redirect to Journey Recovery when file status is not Passed" in {
        when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any(), any())(any(), any()))
          .thenReturn(ResultT.fromValue(orgSubmissionDetailsPassed.copy(fileStatus = Pending)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockSubmissionDetailsConnector, times(1))
            .getSubmissionDetailsByUploadId(eqTo(testUploadId), eqTo(testCarfId))(any(), any())
          verify(mockFileConfirmationHelper, times(0)).rows(any(), any())(any())
          verify(mockSessionRepository, times(0)).set(any())
        }
      }

      "must redirect to Journey Recovery when SubmissionDetailsConnector returns an error" in {
        when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(any(), any())(any(), any()))
          .thenReturn(ResultT.fromError(InternalServerError))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .overrides(bind[FileConfirmationHelper].toInstance(mockFileConfirmationHelper))
          .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, fileConfirmationRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockSubmissionDetailsConnector, times(1))
            .getSubmissionDetailsByUploadId(eqTo(testUploadId), eqTo(testCarfId))(any(), any())
          verify(mockFileConfirmationHelper, times(0)).rows(any(), any())(any())
          verify(mockSessionRepository, times(0)).set(any())
        }
      }
    }
  }
}
