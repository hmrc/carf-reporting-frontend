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

package models.responses

import base.SpecBase

class DisplaySubscriptionContactSpec extends SpecBase {

  "DisplaySubscriptionContact" - {
    ".toSubscriptionContactDetails" - {
      "must return a SubscriptionContactDetails when individual details are present" in {
        val displaySubscriptionContact = DisplaySubscriptionContact(
          individual = Some(displaySubscriptionIndividual),
          organisation = None,
          email = testEmail
        )

        displaySubscriptionContact.toSubscriptionContactDetails mustBe
          Some(SubscriptionContactDetails(testContactName, testEmail))
      }

      "must return a SubscriptionContactDetails when organisation details are present" in {
        val displaySubscriptionContact = DisplaySubscriptionContact(
          individual = None,
          organisation = Some(displaySubscriptionOrganisation),
          email = testEmail
        )

        displaySubscriptionContact.toSubscriptionContactDetails mustBe
          Some(SubscriptionContactDetails(testContactName, testEmail))
      }

      "must return None when neither individual nor organisation details are present" in {
        val displaySubscriptionContact = DisplaySubscriptionContact(
          individual = None,
          organisation = None,
          email = testEmail
        )

        displaySubscriptionContact.toSubscriptionContactDetails mustBe None
      }

      "must return None when both individual and organisation details are present" in {
        val displaySubscriptionContact = DisplaySubscriptionContact(
          individual = Some(displaySubscriptionIndividual),
          organisation = Some(displaySubscriptionOrganisation),
          email = testEmail
        )

        displaySubscriptionContact.toSubscriptionContactDetails mustBe None
      }
    }
  }

}
