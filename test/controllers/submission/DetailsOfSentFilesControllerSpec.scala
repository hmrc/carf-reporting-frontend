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

package controllers.submission

import base.SpecBase
import config.FrontendAppConfig
import connectors.SubmissionDetailsConnector
import models.errors.ApiError.InternalServerError
import models.fileSubmission.SubmissionDetails
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import types.ResultT
import viewmodels.govuk.pagination.PaginationViewModel
import views.html.submission.DetailsOfSentFilesView

class DetailsOfSentFilesControllerSpec extends SpecBase {

  implicit val messages: Messages = messages(app)

  private val mockSubmissionDetailsConnector: SubmissionDetailsConnector = mock[SubmissionDetailsConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSubmissionDetailsConnector)
  }

  "DetailsOfSentFiles Controller" - {

    "must return OK and the correct view when submissions are found (1 page)" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByCarfId(any(), any())(any(), any()))
        .thenReturn(ResultT.fromValue(detailsOfFilesSent))

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, routes.DetailsOfSentFilesController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[DetailsOfSentFilesView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(submissionDetailsListSortedBySubmissionTime :+ submissionHistoryPassed, None, appConfig.managementUrl)(
            request,
            messages(application)
          ).toString

        verify(mockSubmissionDetailsConnector, times(1)).getSubmissionDetailsByCarfId(any(), eqTo(1))(any(), any())
      }
    }

    "must return OK and the correct view when submissions are found (multiple pages)" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByCarfId(any(), any())(any(), any()))
        .thenReturn(ResultT.fromValue(detailsOfFilesSentMultiplePages))

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, routes.DetailsOfSentFilesController.onPageLoad(2).url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[DetailsOfSentFilesView]

        val pagination = PaginationViewModel(
          currentPage = 2,
          totalPages = 3,
          call = page => controllers.submission.routes.DetailsOfSentFilesController.onPageLoad(page)
        )

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          submissionHistoryPassedList(50),
          Some(pagination),
          appConfig.managementUrl
        )(request, messages(application)).toString

        verify(mockSubmissionDetailsConnector, times(1)).getSubmissionDetailsByCarfId(any(), eqTo(2))(any(), any())
      }
    }

    "must redirect to page 1 when the submissions list is empty but totalPages > 0 (page number does not exist)" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByCarfId(any(), any())(any(), any()))
        .thenReturn(ResultT.fromValue(detailsOfFilesSentNoRecords.copy(totalPages = 1)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DetailsOfSentFilesController.onPageLoad(5).url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.DetailsOfSentFilesController.onPageLoad().url

        verify(mockSubmissionDetailsConnector, times(1)).getSubmissionDetailsByCarfId(any(), eqTo(5))(any(), any())
      }
    }

    "must redirect to Journey Recovery when there are no submissions" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByCarfId(any(), any())(any(), any()))
        .thenReturn(ResultT.fromValue(detailsOfFilesSentNoRecords))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DetailsOfSentFilesController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockSubmissionDetailsConnector, times(1)).getSubmissionDetailsByCarfId(any(), eqTo(1))(any(), any())
      }
    }

    "must redirect to Journey Recovery when SubmissionDetailsConnector returns an error" in {
      when(mockSubmissionDetailsConnector.getSubmissionDetailsByCarfId(any(), any())(any(), any()))
        .thenReturn(ResultT.fromError[Seq[SubmissionDetails]](InternalServerError))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionDetailsConnector].toInstance(mockSubmissionDetailsConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DetailsOfSentFilesController.onPageLoad(2).url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockSubmissionDetailsConnector, times(1)).getSubmissionDetailsByCarfId(any(), eqTo(2))(any(), any())
      }
    }
  }
}
