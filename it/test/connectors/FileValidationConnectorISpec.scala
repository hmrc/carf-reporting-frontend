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

import com.github.tomakehurst.wiremock.client.WireMock.*
import itutil.ApplicationWithWiremock
import models.errors.ApiError.{InternalServerError, JsonValidationError}
import models.errors.{InvalidXmlError, XmlError, XmlErrors}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import play.api.http.Status.*

class FileValidationConnectorISpec
    extends ApplicationWithWiremock
    with Matchers
    with ScalaFutures
    with IntegrationPatience {

  lazy val connector: FileValidationConnector = app.injector.instanceOf[FileValidationConnector]

  ".validateUploadedFile" - {

    val testUrl = "/carf-reporting/validate-xml"

    val extractedFileDetailsResponseBody: String =
      s"""
        |{
        |  "messageRefId": "$testMessageRefId",
        |  "sendingEntityIn": "$testRcaspId",
        |  "rcaspName": "$testRcaspName",
        |  "messageTypeIndic": "CARF701",
        |  "hasOtherNexus": false,
        |  "hasCryptoUsers": true,
        |  "docTypeIndic": "OECD10",
        |  "isTestData": true,
        |  "allCryptoUsersAreCorrections": false,
        |  "allCryptoUsersAreDeletions": false
        |}
        |""".stripMargin

    val xmlErrorsResponseBody: String =
      """
        |{
        |  "errors": [
        |    {
        |      "lineNumber": 15,
        |      "errorCode": null,
        |      "errorMessage": "tag name \"MessageTypeIndic\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<Warning>"
        |    },
        |    {
        |      "lineNumber": 17,
        |      "errorCode": null,
        |      "errorMessage": "tag name \"ReportingPeriod\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<MessageTypeIndic>,<Warning>"
        |    },
        |    {
        |      "lineNumber": 18,
        |      "errorCode": null,
        |      "errorMessage": "tag name \"Timestamp\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<MessageTypeIndic>,<ReportingPeriod>,<Warning>"
        |    },
        |    {
        |      "lineNumber": 19,
        |      "errorCode": null,
        |      "errorMessage": "uncompleted content model. expecting: <Contact>,<MessageRefId>,<MessageTypeIndic>,<ReportingPeriod>,<Timestamp>,<Warning>"
        |    }
        |  ],
        |  "_type": "XmlErrors"
        |}
        |""".stripMargin

    "must successfully retrieve an ExtractedFileDetails" in {
      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(extractedFileDetailsResponseBody)
          )
      )

      val result = connector.validateUploadedFile(testDownloadUrl).value.futureValue

      result mustBe Right(extractedFileDetailsTestData)
    }

    "must return JsonValidationError if an unexpected response body is returned with status 200" in {
      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{"not": "the expected shape"}""")
          )
      )

      val result = connector.validateUploadedFile(testDownloadUrl).value.futureValue

      result mustBe Left(JsonValidationError)
    }

    "must successfully retrieve XML schema errors with status 422" in {
      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(UNPROCESSABLE_ENTITY)
              .withBody(xmlErrorsResponseBody)
          )
      )

      val result = connector.validateUploadedFile(testDownloadUrl).value.futureValue

      val expectedXmlErrors = XmlErrors(errors =
        Seq(
          XmlError(
            15,
            "tag name \"MessageTypeIndic\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<Warning>"
          ),
          XmlError(
            17,
            "tag name \"ReportingPeriod\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<MessageTypeIndic>,<Warning>"
          ),
          XmlError(
            18,
            "tag name \"Timestamp\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<MessageTypeIndic>,<ReportingPeriod>,<Warning>"
          ),
          XmlError(
            19,
            "uncompleted content model. expecting: <Contact>,<MessageRefId>,<MessageTypeIndic>,<ReportingPeriod>,<Timestamp>,<Warning>"
          )
        )
      )

      result mustBe Left(expectedXmlErrors)
    }

    "must return InvalidXmlError given a 422 response" in {
      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(UNPROCESSABLE_ENTITY)
              .withBody("""{"_type": "InvalidXmlError"}""")
          )
      )

      val result = connector.validateUploadedFile(testDownloadUrl).value.futureValue

      result mustBe Left(InvalidXmlError)
    }

    "must return JsonValidationError if an unexpected response body is returned with status 422" in {
      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(UNPROCESSABLE_ENTITY)
              .withBody("""{"not": "the expected shape"}""")
          )
      )

      val result = connector.validateUploadedFile(testDownloadUrl).value.futureValue

      result mustBe Left(JsonValidationError)
    }

    "must return InternalServerError given a 400 response" in {
      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
          )
      )

      val result = connector.validateUploadedFile(testDownloadUrl).value.futureValue

      result mustBe Left(InternalServerError)
    }

    "must return InternalServerError given a 500 response" in {
      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result = connector.validateUploadedFile(testDownloadUrl).value.futureValue

      result mustBe Left(InternalServerError)
    }
  }
}
