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
import models.upscan.UploadStatus.*
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.Json

class UpscanConnectorISpec extends ApplicationWithWiremock with Matchers with ScalaFutures with IntegrationPatience {

  lazy val connector: UpscanConnector = app.injector.instanceOf[UpscanConnector]

  ".getUpscanFormData" - {
    val testUrl = "/upscan/v2/initiate"

    "must successfully retrieve an UpscanInitiateResponse" in {
      val preparedUploadJson =
        s"""
          |{
          |  "reference": "${testReference.value}",
          |  "uploadRequest": {
          |      "href": "$postTarget",
          |      "fields": {
          |          "formKey": "formValue"
          |      }
          |  }
          |}
          |""".stripMargin

      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(preparedUploadJson)
          )
      )

      val result = connector.getUpscanFormData(testUploadId).value.futureValue
      result mustBe Right(upscanInitiateResponse)
    }

    "must return JsonValidationError when response JSON is invalid" in {
      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{"incorrect": "structure"}""")
          )
      )

      val result = connector.getUpscanFormData(testUploadId).value.futureValue
      result mustBe Left(JsonValidationError)
    }

    "must return InternalServerError given a 400 response" in {
      val errorResponse = Json.obj(
        "status" -> "Bad request"
      )

      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
              .withBody(errorResponse.toString)
          )
      )

      val result = connector.getUpscanFormData(testUploadId).value.futureValue
      result mustBe Left(InternalServerError)
    }

    "must return InternalServerError given a 500 response" in {
      val errorResponse = Json.obj(
        "status" -> "Internal server error"
      )

      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody(errorResponse.toString)
          )
      )

      val result = connector.getUpscanFormData(testUploadId).value.futureValue
      result mustBe Left(InternalServerError)
    }
  }

  ".requestUpload" - {
    val testUrl = "/carf-reporting/upscan/upload"

    "must return Unit when backend returns 200" in {
      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse().withStatus(OK)
          )
      )

      val result = connector.requestUpload(testUploadId, testReference).value.futureValue
      result mustBe Right((): Unit)
    }

    "must return InternalServerError when backend returns 400" in {
      val errorResponse = Json.obj(
        "status" -> "Bad request"
      )

      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
              .withBody(errorResponse.toString)
          )
      )

      val result = connector.requestUpload(testUploadId, testReference).value.futureValue
      result mustBe Left(InternalServerError)
    }

    "must return InternalServerError when backend returns 500" in {
      val errorResponse = Json.obj(
        "status" -> "Internal server error"
      )

      stubFor(
        post(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody(errorResponse.toString)
          )
      )

      val result = connector.requestUpload(testUploadId, testReference).value.futureValue
      result mustBe Left(InternalServerError)
    }
  }

  ".getUploadStatus" - {
    val testUrl = s"/carf-reporting/upscan/status/${testUploadId.value}"

    "must return an UploadStatus for a valid UploadId" - {
      "when an UploadedSuccessfully response is returned" in {
        val body =
          s"""{
            | "_type": "UploadedSuccessfully",
            | "name": "$testFileName",
            | "downloadUrl": "$testDownloadUrl",
            | "size": $testFileSize,
            | "checksum": "$testChecksum"
            | }
            |""".stripMargin

        stubFor(
          get(urlPathMatching(testUrl))
            .willReturn(
              aResponse().withStatus(OK).withBody(body)
            )
        )

        val result = connector.getUploadStatus(testUploadId).value.futureValue
        result mustBe Right(Some(uploadedSuccessfully))
      }

      "when an UploadRejected response is returned" in {
        val body =
          s"""{
             | "_type": "UploadRejected",
             | "details": {
             |   "failureReason": "REJECTED",
             |   "message": "Error message"
             | }
             |}
             |""".stripMargin

        stubFor(
          get(urlPathMatching(testUrl))
            .willReturn(
              aResponse().withStatus(OK).withBody(body)
            )
        )

        val result = connector.getUploadStatus(testUploadId).value.futureValue
        result mustBe Right(Some(uploadRejected))
      }

      "when a NotStarted response is returned" in {
        val body = """{"_type": "NotStarted"}"""

        stubFor(
          get(urlPathMatching(testUrl))
            .willReturn(
              aResponse().withStatus(OK).withBody(body)
            )
        )

        val result = connector.getUploadStatus(testUploadId).value.futureValue
        result mustBe Right(Some(NotStarted))
      }

      "when a InProgress response is returned" in {
        val body = """{"_type": "InProgress"}"""

        stubFor(
          get(urlPathMatching(testUrl))
            .willReturn(
              aResponse().withStatus(OK).withBody(body)
            )
        )

        val result = connector.getUploadStatus(testUploadId).value.futureValue
        result mustBe Right(Some(InProgress))
      }

      "when a Failed response is returned" in {
        val body = """{"_type": "Failed"}"""

        stubFor(
          get(urlPathMatching(testUrl))
            .willReturn(
              aResponse().withStatus(OK).withBody(body)
            )
        )

        val result = connector.getUploadStatus(testUploadId).value.futureValue
        result mustBe Right(Some(Failed))
      }

      "when a Quarantined response is returned" in {
        val body = """{"_type": "Quarantined"}"""

        stubFor(
          get(urlPathMatching(testUrl))
            .willReturn(
              aResponse().withStatus(OK).withBody(body)
            )
        )

        val result = connector.getUploadStatus(testUploadId).value.futureValue
        result mustBe Right(Some(Quarantined))
      }
    }

    "must return None when the backend returns NOT_FOUND" in {
      stubFor(
        get(urlPathMatching(testUrl))
          .willReturn(
            aResponse().withStatus(NOT_FOUND)
          )
      )

      val result = connector.getUploadStatus(testUploadId).value.futureValue
      result mustBe Right(None)
    }

    "return JsonValidationError when response JSON is invalid" in {
      stubFor(
        get(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{"incorrect": "structure"}""")
          )
      )

      val result = connector.getUploadStatus(testUploadId).value.futureValue
      result mustBe Left(JsonValidationError)
    }

    "return InternalServerError when backend returns 400" in {
      val errorResponse = Json.obj(
        "status" -> "Bad request"
      )

      stubFor(
        get(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
              .withBody(errorResponse.toString)
          )
      )

      val result = connector.getUploadStatus(testUploadId).value.futureValue
      result mustBe Left(InternalServerError)
    }

    "return InternalServerError when backend returns 500" in {
      val errorResponse = Json.obj(
        "status" -> "Internal server error"
      )

      stubFor(
        get(urlPathMatching(testUrl))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody(errorResponse.toString)
          )
      )

      val result = connector.getUploadStatus(testUploadId).value.futureValue
      result mustBe Left(InternalServerError)
    }
  }
}
