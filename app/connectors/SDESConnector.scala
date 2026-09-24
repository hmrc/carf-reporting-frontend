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

import config.FrontendAppConfig
import models.errors.ApiError.InternalServerError
import models.requests.sdes.SubmissionRequest
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.*
import types.ResultT
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.LoggerUtil.{logDebug, logWarn}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SDESConnector @Inject() (val config: FrontendAppConfig, val http: HttpClientV2) {

  def sendSubmission(
      submissionRequest: SubmissionRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): ResultT[Unit] = {

    val baseUrl = url"${config.carfReportingHost}/submit"

    logDebug(
      s"[SDESConnector][sendSubmission] Sending submission uploadId/correlationId: " +
        s"${submissionRequest.uploadId.value}"
    )

    ResultT.fromFuture(
      http
        .post(baseUrl)
        .withBody(Json.toJson(submissionRequest))
        .execute[HttpResponse]
        .map { httpResponse =>
          httpResponse.status match {
            case NO_CONTENT => Right(())
            case _          =>
              logWarn(
                s"[SDESConnector][sendSubmission] Unexpected response. Status code: ${httpResponse.status}, from endpoint: ${baseUrl.toURI}"
              )
              Left(InternalServerError)
          }
        }
    )
  }
}
