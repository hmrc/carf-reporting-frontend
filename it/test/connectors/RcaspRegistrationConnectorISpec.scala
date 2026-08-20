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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlPathMatching}
import itutil.ApplicationWithWiremock
import models.errors.ApiError
import models.responses.RcaspDetails
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import play.api.http.Status.{NOT_FOUND, OK}

class RcaspRegistrationConnectorISpec
    extends ApplicationWithWiremock
    with Matchers
    with ScalaFutures
    with IntegrationPatience {

  lazy val connector: RcaspRegistrationConnector = app.injector.instanceOf[RcaspRegistrationConnector]

  "viewRcasps" - {

    val testUrl = s"/carf-management/view-rcasp/$testCarfId/none"

    val validResponseBody: String =
      """
        |{
        |  "ViewRCASP": {
        |    "ResponseCommon": {
        |      "OriginatingSystem": "MDTP",
        |      "TransmittingSystem": "EIS",
        |      "RequestType": "VIEW",
        |      "Regime": "CARF"
        |    },
        |    "ResponseDetails": {
        |      "RCASPList": [
        |        {
        |          "RCASPID": "ZMCAR0123456787",
        |          "SubscriptionID": "1A30",
        |          "IsRCASPUser": true,
        |          "PartyType": "Organisation",
        |          "RCASPName": "Timmy's Turtles",
        |          "TradingName": "Uva Academy",
        |          "TINDetails": [
        |            { "TINType": "UTR", "TIN": "1111111111", "IssuedBy": "GB" }
        |          ],
        |          "AddressDetails": {
        |            "AddressLine1": "1 Test",
        |            "AddressLine2": "Test Street",
        |            "AddressLine3": "Test Region",
        |            "AddressLine4": "Testingtown",
        |            "PostalCode": "B23 2AZ",
        |            "CountryCode": "GB"
        |          }
        |        },
        |        {
        |          "RCASPID": "ZMCAR0123456788",
        |          "SubscriptionID": "1A30",
        |          "IsRCASPUser": false,
        |          "PartyType": "Individual",
        |          "FirstName": "Nemona",
        |          "LastName": "Champion",
        |          "TINDetails": [
        |            { "TINType": "OTHER", "TIN": "1111111111", "IssuedBy": "GB" }
        |          ],
        |          "AddressDetails": {
        |            "AddressLine1": "1 Test",
        |            "AddressLine2": "Test Street",
        |            "AddressLine3": "Test Region",
        |            "AddressLine4": "Testingtown",
        |            "PostalCode": "B23 2AZ",
        |            "CountryCode": "GB"
        |          },
        |          "PrimaryContactDetails": {
        |            "ContactName": "Nemona Champion",
        |            "EmailAddress": "john.doe@example.com"
        |          }
        |        },
        |        {
        |          "RCASPID": "ZMCAR0123456786",
        |          "SubscriptionID": "1A30",
        |          "IsRCASPUser": false,
        |          "PartyType": "Organisation",
        |          "RCASPName": "Timmy's Turtles",
        |          "TradingName": "Uva Academy",
        |          "TINDetails": [
        |            { "TINType": "UTR", "TIN": "1111111111", "IssuedBy": "GB" }
        |          ],
        |          "AddressDetails": {
        |            "AddressLine1": "1 Test",
        |            "AddressLine2": "Test Street",
        |            "AddressLine3": "Test Region",
        |            "AddressLine4": "Testingtown",
        |            "PostalCode": "B23 2AZ",
        |            "CountryCode": "GB"
        |          },
        |          "PrimaryContactDetails": {
        |            "ContactName": "Nemona Champion",
        |            "EmailAddress": "john.doe@example.com"
        |          },
        |          "SecondaryContactDetails": {
        |            "ContactName": "Clavell",
        |            "EmailAddress": "clavell@uva.edu.org"
        |          }
        |        }
        |      ]
        |    }
        |  }
        |}
        |""".stripMargin

    "must successfully retrieve a list of RcaspDetails, ignoring fields not required by RcaspDetails" in {
      stubFor(
        get(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(validResponseBody)
          )
      )

      val result = connector.viewRcasps(testCarfId).value.futureValue

      result mustBe Right(
        List(organisationRegisteredBusinessRcaspDetails, individualRcaspDetails, organisationStandardRcaspDetails)
      )
    }

    "must return a Json validation error if an unexpected response body is returned" in {
      stubFor(
        get(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{"not": "the expected shape"}""")
          )
      )

      val result = connector.viewRcasps(testCarfId).value.futureValue

      result mustBe Left(ApiError.JsonValidationError)
    }

    "must return an empty list if the backend returns 404" in {
      stubFor(
        get(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result = connector.viewRcasps(testCarfId).value.futureValue

      result mustBe Right(List.empty)
    }

    "must return an internal server error if an unexpected non-200 status is returned" in {
      stubFor(
        get(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(500)
          )
      )

      val result = connector.viewRcasps(testCarfId).value.futureValue

      result mustBe Left(ApiError.InternalServerError)
    }
  }
}
