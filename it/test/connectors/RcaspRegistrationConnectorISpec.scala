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
import models.rcasp.RcaspDetails
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import play.api.http.Status.{NOT_FOUND, OK}

class RcaspRegistrationConnectorISpec
    extends ApplicationWithWiremock
    with Matchers
    with ScalaFutures
    with IntegrationPatience {

  lazy val connector: RcaspRegistrationConnector = app.injector.instanceOf[RcaspRegistrationConnector]

  val testCarfIdLocal = "XE0000123456789"

  val validResponseBody: String =
    """
      |{
      |  "ViewRCASP": {
      |    "ResponseCommon": {
      |      "OriginatingSystem": "MDTP",
      |      "TransmittingSystem": "EIS",
      |      "RequestType": "VIEW",
      |      "Regime": "CARF",
      |      "ResponseParameters": null
      |    },
      |    "ResponseDetails": {
      |      "RCASPList": [
      |        { "RCASPID": "ZMCAR0123456787" },
      |        { "RCASPID": "ZMCAR0123456788" }
      |      ]
      |    }
      |  }
      |}
      |""".stripMargin

  "viewRcasps" - {

    "must successfully retrieve a list of RcaspDetails" in {
      stubFor(
        get(urlPathMatching(s"/carf-management/view-rcasp/$testCarfIdLocal/none"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(validResponseBody)
          )
      )

      val result = connector.viewRcasps(testCarfIdLocal).value.futureValue

      result shouldBe Right(List(RcaspDetails("ZMCAR0123456787"), RcaspDetails("ZMCAR0123456788")))
    }

    "must return a Json validation error if an unexpected response body is returned" in {
      stubFor(
        get(urlPathMatching(s"/carf-management/view-rcasp/$testCarfIdLocal/none"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{"not": "the expected shape"}""")
          )
      )

      val result = connector.viewRcasps(testCarfIdLocal).value.futureValue

      result shouldBe Left(ApiError.JsonValidationError)
    }

    "must return an empty list if the backend returns 404" in {
      stubFor(
        get(urlPathMatching(s"/carf-management/view-rcasp/$testCarfIdLocal/none"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result = connector.viewRcasps(testCarfIdLocal).value.futureValue

      result shouldBe Right(List.empty)
    }

    "must return an internal server error if an unexpected non-200 status is returned" in {
      stubFor(
        get(urlPathMatching(s"/carf-management/view-rcasp/$testCarfIdLocal/none"))
          .willReturn(
            aResponse()
              .withStatus(500)
          )
      )

      val result = connector.viewRcasps(testCarfIdLocal).value.futureValue

      result shouldBe Left(ApiError.InternalServerError)
    }
  }
}
