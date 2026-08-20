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
import connectors.{RcaspRegistrationConnector, SubscriptionConnector}
import models.errors.ApiError.InternalServerError
import models.responses.{DisplaySubscriptionContact, DisplaySubscriptionDetails, DisplaySubscriptionResponse, DisplaySubscriptionSuccess}
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.{reset, times, verify, when}
import pages.{ExtractedFileDetailsPage, RcaspDetailsPage, SubscriptionDetailsPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import types.ResultT

import scala.concurrent.Future

class RcaspAndSubscriptionDetailsControllerSpec extends SpecBase {

  val mockRcaspConnector: RcaspRegistrationConnector   = mock[RcaspRegistrationConnector]
  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRcaspConnector, mockSubscriptionConnector)
  }

  lazy val rcaspAndSubscriptionDetailsRoute: String = routes.RcaspAndSubscriptionDetailsController.onPageLoad().url

  "RcaspAndSubscriptionDetails Controller" - {
    "when the RCASP list contains the sendingEntityIn in the file" - {
      val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)

      "must redirect to CheckYourFileDetails given a valid DisplaySubscriptionResponse" in {
        when(mockRcaspConnector.viewRcasps(any())(any(), any()))
          .thenReturn(ResultT.fromValue(List(organisationRegisteredBusinessRcaspDetails)))
        when(mockSubscriptionConnector.displaySubscription(any())(any(), any()))
          .thenReturn(ResultT.fromValue(displaySubscriptionResponseOrganisation))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[RcaspRegistrationConnector].toInstance(mockRcaspConnector),
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, rcaspAndSubscriptionDetailsRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.CheckYourFileDetailsController.onPageLoad().url

          verify(mockRcaspConnector, times(1)).viewRcasps(any())(any(), any())
          verify(mockSubscriptionConnector, times(1)).displaySubscription(any())(any(), any())
          verify(mockSessionRepository, times(1)).set(argThat { ua =>
            ua.get(RcaspDetailsPage).contains(organisationRegisteredBusinessRcaspDetails) &&
            ua.get(SubscriptionDetailsPage).contains(subscriptionDetailsOrganisation)
          })
        }
      }

      "must redirect to Journey Recovery when unable to create SubscriptionDetails from the DisplaySubscriptionResponse" in {
        val badDisplaySubscriptionResponse = DisplaySubscriptionResponse(
          success = DisplaySubscriptionSuccess(
            processingDate = "2024-01-25T09:26:17Z",
            carfSubscriptionDetails = DisplaySubscriptionDetails(
              carfReference = testCarfId,
              primaryContact = DisplaySubscriptionContact(
                individual = None,
                organisation = None,
                email = "GroupRep@FATCACRS.com"
              ),
              secondaryContact = None
            )
          )
        )

        when(mockRcaspConnector.viewRcasps(any())(any(), any()))
          .thenReturn(ResultT.fromValue(List(organisationRegisteredBusinessRcaspDetails)))
        when(mockSubscriptionConnector.displaySubscription(any())(any(), any()))
          .thenReturn(ResultT.fromValue(badDisplaySubscriptionResponse))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[RcaspRegistrationConnector].toInstance(mockRcaspConnector),
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, rcaspAndSubscriptionDetailsRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockRcaspConnector, times(1)).viewRcasps(any())(any(), any())
          verify(mockSubscriptionConnector, times(1)).displaySubscription(any())(any(), any())
          verify(mockSessionRepository, times(0)).set(any())
        }
      }

      "must redirect to Journey Recovery when SubscriptionConnector returns an error" in {
        when(mockRcaspConnector.viewRcasps(any())(any(), any()))
          .thenReturn(ResultT.fromValue(List(organisationRegisteredBusinessRcaspDetails)))
        when(mockSubscriptionConnector.displaySubscription(any())(any(), any()))
          .thenReturn(ResultT.fromError(InternalServerError))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[RcaspRegistrationConnector].toInstance(mockRcaspConnector),
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, rcaspAndSubscriptionDetailsRoute)
          val result  = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(mockRcaspConnector, times(1)).viewRcasps(any())(any(), any())
          verify(mockSubscriptionConnector, times(1)).displaySubscription(any())(any(), any())
          verify(mockSessionRepository, times(0)).set(any())
        }
      }
    }

    "must redirect to RcaspNotMatchingController when the RCASP list does not contain the sendingEntityIn in the file" in {
      val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)

      when(mockRcaspConnector.viewRcasps(any())(any(), any()))
        .thenReturn(ResultT.fromValue(List(individualRcaspDetails)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[RcaspRegistrationConnector].toInstance(mockRcaspConnector),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, rcaspAndSubscriptionDetailsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.RcaspNotMatchingController.onPageLoad().url

        verify(mockRcaspConnector, times(1)).viewRcasps(any())(any(), any())
        verify(mockSubscriptionConnector, times(0)).displaySubscription(any())(any(), any())
        verify(mockSessionRepository, times(0)).set(any())
      }
    }

    "must redirect to Journey Recovery when RcaspRegistrationConnector returns an error" in {
      val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)

      when(mockRcaspConnector.viewRcasps(any())(any(), any())).thenReturn(ResultT.fromError(InternalServerError))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[RcaspRegistrationConnector].toInstance(mockRcaspConnector),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, rcaspAndSubscriptionDetailsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRcaspConnector, times(1)).viewRcasps(any())(any(), any())
        verify(mockSubscriptionConnector, times(0)).displaySubscription(any())(any(), any())
        verify(mockSessionRepository, times(0)).set(any())
      }
    }

    "must redirect to Journey Recovery when ExtractedFileDetails is missing from user answers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[RcaspRegistrationConnector].toInstance(mockRcaspConnector),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, rcaspAndSubscriptionDetailsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRcaspConnector, times(0)).viewRcasps(any())(any(), any())
        verify(mockSubscriptionConnector, times(0)).displaySubscription(any())(any(), any())
        verify(mockSessionRepository, times(0)).set(any())
      }
    }

    "must redirect to Journey Recovery when user answers do not exist" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, rcaspAndSubscriptionDetailsRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
