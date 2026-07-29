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

package testOnly.controllers

import base.SpecBase
import connectors.RcaspRegistrationConnector
import models.errors.ApiError.InternalServerError
import models.rcasp.RcaspDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import testOnly.controllers.RcaspValidationController
import types.ResultT

import scala.concurrent.Future

class RcaspValidationControllerSpec extends SpecBase {

  private val mockRcaspRegistrationConnector: RcaspRegistrationConnector = mock[RcaspRegistrationConnector]

  private val matchingRcaspId    = "ZMCAR0123456788"
  private val nonMatchingRcaspId = "NOTAREALID123"

  private val rcaspList = List(RcaspDetails("ZMCAR0123456787"), RcaspDetails(matchingRcaspId))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRcaspRegistrationConnector)
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  "RcaspValidationController" - {

    "onPageLoad" - {

      "must redirect to Journey Recovery when sendingEntityIn is not supplied, without calling the connector" in {
        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[RcaspRegistrationConnector].toInstance(mockRcaspRegistrationConnector))
          .build()

        running(application) {
          val controller = application.injector.instanceOf[RcaspValidationController]
          val request    = FakeRequest(GET, "/test-only/validate-file")

          val result = controller.onPageLoad(None)(request)

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockRcaspRegistrationConnector, never()).viewRcasps(any())(any(), any())
          verify(mockSessionRepository, never()).set(any())
        }
      }

      "must redirect to the placeholder page when sendingEntityIn matches a returned RCASPID" in {
        when(mockRcaspRegistrationConnector.viewRcasps(any())(any(), any()))
          .thenReturn(ResultT.fromValue(rcaspList))

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[RcaspRegistrationConnector].toInstance(mockRcaspRegistrationConnector))
          .build()

        running(application) {
          val controller = application.injector.instanceOf[RcaspValidationController]
          val request    = FakeRequest(GET, "/test-only/validate-file")

          val result = controller.onPageLoad(Some(matchingRcaspId))(request)

          status(result)            mustEqual SEE_OTHER
          redirectLocation(result).value must include("placeholder")

          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "must redirect to /problem/rcasp-not-matching when sendingEntityIn does not match any returned RCASPID" in {
        when(mockRcaspRegistrationConnector.viewRcasps(any())(any(), any()))
          .thenReturn(ResultT.fromValue(rcaspList))

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[RcaspRegistrationConnector].toInstance(mockRcaspRegistrationConnector))
          .build()

        running(application) {
          val controller = application.injector.instanceOf[RcaspValidationController]
          val request    = FakeRequest(GET, "/test-only/validate-file")

          val result = controller.onPageLoad(Some(nonMatchingRcaspId))(request)

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.problem.routes.RcaspNotMatchingController.onPageLoad().url

          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "must redirect to Journey Recovery when the connector returns an error" in {
        when(mockRcaspRegistrationConnector.viewRcasps(any())(any(), any()))
          .thenReturn(ResultT.fromError(InternalServerError))

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[RcaspRegistrationConnector].toInstance(mockRcaspRegistrationConnector))
          .build()

        running(application) {
          val controller = application.injector.instanceOf[RcaspValidationController]
          val request    = FakeRequest(GET, "/test-only/validate-file")

          val result = controller.onPageLoad(Some(matchingRcaspId))(request)

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockSessionRepository, never()).set(any())
        }
      }
    }
  }
}
