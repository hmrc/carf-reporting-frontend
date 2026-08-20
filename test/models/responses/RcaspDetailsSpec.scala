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

class RcaspDetailsSpec extends SpecBase {

  "RcaspDetails" - {
    ".getName" - {
      "given an IndividualRcaspDetails" in {
        individualRcaspDetails.getName mustBe testContactName
      }

      "given an OrganisationRcaspDetailsRcaspUser" in {
        organisationRegisteredBusinessRcaspDetails.getName mustBe testRcaspName
      }

      "given an OrganisationRcaspDetailsStandard" in {
        organisationStandardRcaspDetails.getName mustBe testRcaspName
      }
    }

    ".getEmails" - {
      "given an IndividualRcaspDetails" in {
        individualRcaspDetails.getEmails mustBe List(testEmail)
      }

      "given an OrganisationRcaspDetailsRcaspUser" in {
        organisationRegisteredBusinessRcaspDetails.getEmails mustBe List.empty
      }

      "given an OrganisationRcaspDetailsStandard" - {
        "when there are primary and secondary contact details" in {
          organisationStandardRcaspDetails.getEmails mustBe List(testEmail, "clavell@uva.edu.org")
        }

        "when there are only primary contact details" in {
          organisationStandardRcaspDetails.copy(SecondaryContactDetails = None).getEmails mustBe List(testEmail)
        }
      }
    }
  }

}
