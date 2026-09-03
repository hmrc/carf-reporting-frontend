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

package controllers.actions

import base.SpecBase
import controllers.routes
import models.UserAnswers
import models.requests.OptionalDataRequest
import pages.UploadCompletionLockPage
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class UploadCompletionLockActionSpec extends SpecBase {

  class Harness extends UploadCompletionLockAction {
    def callFilter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = filter(request)
  }

  private def fakeRequest = FakeRequest("", "")

  private def buildRequest(userAnswers: Option[UserAnswers]): OptionalDataRequest[_] =
    OptionalDataRequest(fakeRequest, testInternalId, userAnswers, testCarfId)

  "UploadCompletionLockAction" - {

    "must return None to let the request continue" - {

      "when there are no user answers in the request" in {
        val harnessAction = new Harness()
        val request       = buildRequest(userAnswers = None)

        val result = harnessAction.callFilter(request).futureValue

        result mustBe empty
      }

      "when user answers exist but UploadCompletionLockPage is missing" in {
        val harnessAction = new Harness()
        val request       = buildRequest(userAnswers = Some(emptyUserAnswers))

        val result = harnessAction.callFilter(request).futureValue

        result mustBe empty
      }

      "when user answers exist and UploadCompletionLockPage is false" in {
        val harnessAction = new Harness()
        val userAnswers   = emptyUserAnswers.set(UploadCompletionLockPage, false).success.value
        val request       = buildRequest(userAnswers = Some(userAnswers))

        val result = harnessAction.callFilter(request).futureValue

        result mustBe empty
      }
    }

    "must block the request and redirect" - {

      "when user answers exist and UploadCompletionLockPage is true" in {
        val harnessAction = new Harness()
        val userAnswers   = emptyUserAnswers.withPage(UploadCompletionLockPage, true)
        val request       = buildRequest(userAnswers = Some(userAnswers))

        val result = harnessAction.callFilter(request).futureValue

        result mustBe defined

        val redirectResult = Future.successful(result.get)

        status(redirectResult)                 mustEqual SEE_OTHER
        redirectLocation(redirectResult).value mustEqual routes.PlaceholderController
          .onPageLoad("Should nav to /problem/page-unavailable (CARF-308)")
          .url
      }
    }
  }
}
