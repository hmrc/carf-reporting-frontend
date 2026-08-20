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

class DisplaySubscriptionResponseSpec extends SpecBase {

  "DisplaySubscriptionResponse" - {
    ".toSubscriptionDetails" - {
      "must return a SubscriptionDetails when primary and secondary user details are present and valid" in {
        displaySubscriptionResponseOrganisation.toSubscriptionDetails mustBe Some(subscriptionDetailsOrganisation)
      }

      "must return a SubscriptionDetails when primary user details is valid and secondary user details is absent" in {
        displaySubscriptionResponseIndividual.toSubscriptionDetails mustBe Some(subscriptionDetailsIndividual)
      }

      "must return None when primary user details is valid but secondary user details is invalid" in {
        val displaySubscriptionResponse = DisplaySubscriptionResponse(
          success = DisplaySubscriptionSuccess(
            processingDate = "2024-01-25T09:26:17Z",
            carfSubscriptionDetails = DisplaySubscriptionDetails(
              carfReference = testCarfId,
              primaryContact = DisplaySubscriptionContact(
                individual = None,
                organisation = Some(DisplaySubscriptionOrganisation(name = "John Doe")),
                email = "GroupRep@FATCACRS.com"
              ),
              secondaryContact = Some(
                DisplaySubscriptionContact(
                  individual = None,
                  organisation = None,
                  email = "GroupRep2@FATCACRS.com"
                )
              )
            )
          )
        )

        displaySubscriptionResponse.toSubscriptionDetails mustBe None
      }

      "must return None when primary user details is invalid" - {
        "when secondary user details is absent" in {
          val displaySubscriptionResponse = DisplaySubscriptionResponse(
            success = DisplaySubscriptionSuccess(
              processingDate = "2024-01-25T09:26:17Z",
              carfSubscriptionDetails = DisplaySubscriptionDetails(
                carfReference = testCarfId,
                primaryContact = DisplaySubscriptionContact(
                  individual = Some(
                    DisplaySubscriptionIndividual(
                      firstName = "Joe",
                      lastName = "Smith"
                    )
                  ),
                  organisation = Some(DisplaySubscriptionOrganisation(name = "John Doe")),
                  email = "GroupRep@FATCACRS.com"
                ),
                secondaryContact = None
              )
            )
          )

          displaySubscriptionResponse.toSubscriptionDetails mustBe None
        }

        "when secondary user details is valid" in {
          val displaySubscriptionResponse = DisplaySubscriptionResponse(
            success = DisplaySubscriptionSuccess(
              processingDate = "2024-01-25T09:26:17Z",
              carfSubscriptionDetails = DisplaySubscriptionDetails(
                carfReference = testCarfId,
                primaryContact = DisplaySubscriptionContact(
                  individual = None,
                  organisation = None,
                  email = "GroupRep@FATCACRS.com"
                ),
                secondaryContact = Some(
                  DisplaySubscriptionContact(
                    individual = None,
                    organisation = Some(DisplaySubscriptionOrganisation(name = "Jane Doe")),
                    email = "GroupRep2@FATCACRS.com"
                  )
                )
              )
            )
          )

          displaySubscriptionResponse.toSubscriptionDetails mustBe None

        }

        "when secondary user details is invalid" in {
          val displaySubscriptionResponse = DisplaySubscriptionResponse(
            success = DisplaySubscriptionSuccess(
              processingDate = "2024-01-25T09:26:17Z",
              carfSubscriptionDetails = DisplaySubscriptionDetails(
                carfReference = testCarfId,
                primaryContact = DisplaySubscriptionContact(
                  individual = None,
                  organisation = None,
                  email = "GroupRep@FATCACRS.com"
                ),
                secondaryContact = Some(
                  DisplaySubscriptionContact(
                    individual = None,
                    organisation = None,
                    email = "GroupRep2@FATCACRS.com"
                  )
                )
              )
            )
          )

          displaySubscriptionResponse.toSubscriptionDetails mustBe None
        }
      }
    }
  }

}
