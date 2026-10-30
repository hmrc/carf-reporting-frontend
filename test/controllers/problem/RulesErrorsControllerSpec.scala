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

package controllers.problem

import base.SpecBase
import config.FrontendAppConfig
import connectors.SubmissionDetailsConnector
import models.errors.ApiError.InternalServerError
import models.errors.{BusinessRuleValidationErrors, FileError, RecordError}
import models.problem.BusinessRuleError
import models.problem.MessageBlock.Para
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import types.ResultT
import utils.ErrorContentMapper.getContentForError
import views.html.problem.RulesErrorsView

import scala.util.Try

class RulesErrorsControllerSpec extends SpecBase {

  private val mockSubmissionDetailsConnector: SubmissionDetailsConnector = mock[SubmissionDetailsConnector]

  private def processTestBusinessRuleErrors(
      businessRuleErrors: BusinessRuleValidationErrors
  ): Seq[BusinessRuleError] = {

    val processedFileErrors = businessRuleErrors.fileError.map { error =>
      getContentForError(error.code).fold(
        BusinessRuleError(error.code, Seq("N/A"), Seq(Para(error.details.getOrElse(""))))
      )(identity)
    }

    val processedRecordErrors = businessRuleErrors.recordError.map { error =>
      getContentForError(error.code).fold(
        BusinessRuleError(error.code, error.docRefIDInError, Seq(Para(error.details.getOrElse(""))))
      ) { businessRuleError =>
        if (error.docRefIDInError.nonEmpty) {
          businessRuleError.copy(docRefIds = error.docRefIDInError)
        } else {
          businessRuleError
        }
      }
    }

    val combinedErrors = processedFileErrors ++ processedRecordErrors
    Try(combinedErrors.sortBy(_.errorCode.toInt)).getOrElse(combinedErrors.sortBy(_.errorCode))
  }

  private val belowMaxErrors   = processTestBusinessRuleErrors(businessRuleValidationErrors)
  private val exactlyMaxErrors = processTestBusinessRuleErrors(businessRuleValidationManyErrors(100))
  private val aboveMaxErrors   = processTestBusinessRuleErrors(businessRuleValidationManyErrors(150))

  lazy val rulesErrorsRoute: String = routes.RulesErrorsController.onPageLoad(testUploadId.value).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSubmissionDetailsConnector)
  }

  "RulesErrors Controller" - {

    "return OK and the correct view when errors and filename are both present, below the max" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any()))
        .thenReturn(ResultT.fromValue(submissionDetailsFailed))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, rulesErrorsRoute)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[RulesErrorsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(testFileName, belowMaxErrors, hasMoreThanMax = false, appConfig.managementUrl)(
            request,
            messages(application)
          ).toString

        verify(mockSubmissionDetailsConnector, times(1))
          .getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any())
      }
    }

    "return OK and fallback to default details when error code is unknown" in {
      val unknownErrors = BusinessRuleValidationErrors(
        fileError = Seq(FileError("UNKNOWN_1", Some("Unknown file detail"))),
        recordError = Seq(RecordError("UNKNOWN_2", Some("Unknown record detail"), Seq("docRef123")))
      )

      when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any()))
        .thenReturn(
          ResultT.fromValue(submissionDetailsFailed.copy(businessRuleErrors = unknownErrors))
        )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, rulesErrorsRoute)
        val result    = route(application, request).value
        val view      = application.injector.instanceOf[RulesErrorsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(
            testFileName,
            processTestBusinessRuleErrors(unknownErrors),
            hasMoreThanMax = false,
            appConfig.managementUrl
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "return OK and use mapped docRefIds when record error docRefIDInError is empty" in {
      val emptyDocRefRecordError = RecordError("17", Some("Details"), Seq.empty)
      val errorsWithEmptyDocRef  = BusinessRuleValidationErrors(Seq.empty, Seq(emptyDocRefRecordError))

      when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any()))
        .thenReturn(
          ResultT.fromValue(submissionDetailsFailed.copy(businessRuleErrors = errorsWithEmptyDocRef))
        )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, rulesErrorsRoute)
        val result    = route(application, request).value
        val view      = application.injector.instanceOf[RulesErrorsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(
            testFileName,
            processTestBusinessRuleErrors(errorsWithEmptyDocRef),
            hasMoreThanMax = false,
            appConfig.managementUrl
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "return OK and not show the over-100 message when errors equal the max" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any()))
        .thenReturn(
          ResultT.fromValue(
            submissionDetailsFailed.copy(businessRuleErrors = businessRuleValidationManyErrors(100))
          )
        )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, rulesErrorsRoute)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[RulesErrorsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(testFileName, exactlyMaxErrors, hasMoreThanMax = false, appConfig.managementUrl)(
            request,
            messages(application)
          ).toString

        verify(mockSubmissionDetailsConnector, times(1))
          .getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any())
      }
    }

    "return OK and truncate to 100 rows with hasMoreThanMax = true when errors exceed the max" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any()))
        .thenReturn(
          ResultT.fromValue(
            submissionDetailsFailed.copy(businessRuleErrors = businessRuleValidationManyErrors(150))
          )
        )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, rulesErrorsRoute)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[RulesErrorsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(
            testFileName,
            aboveMaxErrors.take(100),
            hasMoreThanMax = true,
            appConfig.managementUrl
          )(
            request,
            messages(application)
          ).toString

        verify(mockSubmissionDetailsConnector, times(1))
          .getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any())
      }
    }

    "redirect to Journey Recovery when file status is not Failed" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any()))
        .thenReturn(ResultT.fromValue(submissionDetailsPending))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, rulesErrorsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockSubmissionDetailsConnector, times(1))
          .getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any())
      }
    }

    "redirect to Journey Recovery when businessRuleValidationErrors contains no errors" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any()))
        .thenReturn(
          ResultT.fromValue(
            submissionDetailsFailed.copy(businessRuleErrors = BusinessRuleValidationErrors.apply())
          )
        )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, rulesErrorsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockSubmissionDetailsConnector, times(1))
          .getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any())
      }
    }

    "redirect to Journey Recovery when SubmissionDetailsConnector returns an error" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any()))
        .thenReturn(ResultT.fromError(InternalServerError))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, rulesErrorsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockSubmissionDetailsConnector, times(1))
          .getSubmissionDetailsByUploadId(eqTo(testUploadId))(any(), any())
      }
    }
  }
}
