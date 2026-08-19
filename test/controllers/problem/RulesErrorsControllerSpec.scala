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
import models.problem.{BusinessRuleError, RulesErrorsStubData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.RulesErrorsStubService
import views.html.problem.RulesErrorsView

class RulesErrorsControllerSpec extends SpecBase {

  private val mockService: RulesErrorsStubService = mock[RulesErrorsStubService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockService)
  }

  "RulesErrors Controller" - {

    "must return OK and the correct view when errors and filename are both present, under the max" in {

      when(mockService.getFileName(any())).thenReturn(Some("filename.xml"))
      when(mockService.getRulesErrors(any())).thenReturn(Some(RulesErrorsStubData.fewErrors))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[RulesErrorsStubService].toInstance(mockService))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, routes.RulesErrorsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RulesErrorsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view("filename.xml", RulesErrorsStubData.fewErrors, hasMoreThanMax = false, appConfig.managementUrl)(
            request,
            messages(application)
          ).toString
      }
    }

    "must return OK and truncate to 100 rows with hasMoreThanMax true when errors exceed the max" in {

      when(mockService.getFileName(any())).thenReturn(Some("filename.xml"))
      when(mockService.getRulesErrors(any())).thenReturn(Some(RulesErrorsStubData.manyErrors))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[RulesErrorsStubService].toInstance(mockService))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, routes.RulesErrorsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RulesErrorsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual
          view(
            "filename.xml",
            RulesErrorsStubData.manyErrors.take(100),
            hasMoreThanMax = true,
            appConfig.managementUrl
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to Journey Recovery when both errors and filename are missing" in {

      when(mockService.getFileName(any())).thenReturn(None)
      when(mockService.getRulesErrors(any())).thenReturn(None)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[RulesErrorsStubService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RulesErrorsController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when filename is missing but errors are present" in {

      when(mockService.getFileName(any())).thenReturn(None)
      when(mockService.getRulesErrors(any())).thenReturn(Some(RulesErrorsStubData.fewErrors))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[RulesErrorsStubService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RulesErrorsController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when errors are empty but filename is present" in {

      when(mockService.getFileName(any())).thenReturn(Some("filename.xml"))
      when(mockService.getRulesErrors(any())).thenReturn(Some(Seq.empty[BusinessRuleError]))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[RulesErrorsStubService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RulesErrorsController.onPageLoad().url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
