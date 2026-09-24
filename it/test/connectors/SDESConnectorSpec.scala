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
import models.errors.ApiError.InternalServerError
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

class SDESConnectorSpec extends ApplicationWithWiremock
  with Matchers
  with ScalaFutures
  with IntegrationPatience {

  lazy val connector: SDESConnector = app.injector.instanceOf[SDESConnector]

  override implicit val hc: HeaderCarrier = HeaderCarrier()

  val testSubmissionRequestJson: String =
    s"""
       |{
       |  "fileName": "test-file.xml",
       |  "uploadId": "${testUploadId.value}",
       |  "fileSize": 1024,
       |  "documentUrl": "http://localhost:8080/file",
       |  "checksum": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
       |  "rcaspDetails": {
       |    "RCASPID": "ZMCAR0123456788",
       |    "IsRCASPUser": false,
       |    "FirstName": "testFirstName",
       |    "LastName": "testLastName",
       |    "PrimaryContactDetails": {
       |      "ContactName": "testContactName",
       |      "EmailAddress": "test@example.com"
       |    }
       |  },
       |  "subscriptionDetails": {
       |    "carfReference": "XACARF000001234",
       |    "gbUser": true,
       |    "primaryContact": {
       |      "individual": {
       |        "firstName": "Jane",
       |        "lastName": "Smith"
       |      },
       |      "email": "GroupRep@FATCACRS.com"
       |    }
       |  },
       |  "extractedFileDetails": {
       |    "messageRefId": "${extractedFileDetailsCarf.messageRefId}",
       |    "sendingEntityIn": "${extractedFileDetailsCarf.sendingEntityIn}",
       |    "rcaspName": "${extractedFileDetailsCarf.rcaspName.get}",
       |    "messageTypeIndic": "${extractedFileDetailsCarf.messageTypeIndic}",
       |    "hasOtherNexus": ${extractedFileDetailsCarf.hasOtherNexus.toString},
       |    "hasCryptoUsers": ${extractedFileDetailsCarf.hasCryptoUsers.toString},
       |    "docTypeIndic": "${extractedFileDetailsCarf.docTypeIndic.get}",
       |    "isTestData": ${extractedFileDetailsCarf.isTestData},
       |    "allCryptoUsersAreCorrections": ${extractedFileDetailsCarf.allCryptoUsersAreCorrections},
       |    "allCryptoUsersAreDeletions": ${extractedFileDetailsCarf.allCryptoUsersAreDeletions}
       |  }
       |}
       |""".stripMargin

  "SDESConnector" - {
    "sendSubmission" - {
      "return Right(()) when the downstream service returns NO_CONTENT (204)" in {
        stubFor(
          post(urlPathMatching("/submit"))
            .willReturn(
              aResponse()
                .withBody(Json.parse(testSubmissionRequestJson).toString)
                .withStatus(NO_CONTENT)
            )
        )

        val result = connector.sendSubmission(testSubmissionRequest).value.futureValue

        result mustBe Right(())
      }

      "return Left(InternalServerError) when the downstream service returns an unexpected status (e.g., 400)" in {
        stubFor(
          post(urlPathMatching("/submit"))
            .willReturn(
              aResponse()
                .withBody(Json.parse(testSubmissionRequestJson).toString)
                .withStatus(BAD_REQUEST)
            )
        )

        val result = connector.sendSubmission(testSubmissionRequest).value.futureValue

        result mustBe Left(InternalServerError)
      }

      "return Left(InternalServerError) when the downstream service returns an unexpected status (e.g., 500)" in {
        stubFor(
          post(urlEqualTo("/submit"))
            .willReturn(
              aResponse()
                .withBody(Json.parse(testSubmissionRequestJson).toString)
                .withStatus(INTERNAL_SERVER_ERROR)
            )
        )

        val result = connector.sendSubmission(testSubmissionRequest).value.futureValue

        result mustBe Left(InternalServerError)
      }
    }
  }
}