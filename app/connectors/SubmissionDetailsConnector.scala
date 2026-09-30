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
import models.errors.ApiError.{InternalServerError, JsonValidationError, NotFoundError}
import models.fileSubmission.SubmissionDetails
import models.upscan.UploadId
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.*
import types.ResultT
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.LoggerUtil.*

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class SubmissionDetailsConnector @Inject() (httpClient: HttpClientV2, config: FrontendAppConfig) {

  def getSubmissionDetailsByUploadId(
      uploadId: UploadId
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): ResultT[SubmissionDetails] = {
    val requestUrl = url"${config.carfReportingBaseUrl}/submission-details/${uploadId.value}"

    logInfo(
      s"[SubmissionDetailsConnector][getSubmissionDetailsByUploadId] Getting submission details for uploadId ${uploadId.value}"
    )

    ResultT.fromFuture {
      httpClient
        .get(requestUrl)
        .execute[HttpResponse]
        .map { response =>
          response.status match {
            case OK        =>
              Try(response.json.as[SubmissionDetails]) match {
                case Success(fileDetails) => Right(fileDetails)
                case Failure(_)           =>
                  logWarn(
                    s"[SubmissionDetailsConnector][getSubmissionDetailsByUploadId] Error parsing response body for uploadId ${uploadId.value}"
                  )
                  Left(JsonValidationError)
              }
            case NOT_FOUND =>
              logWarn(
                s"[SubmissionDetailsConnector][getSubmissionDetailsByUploadId] No submission details found for uploadId ${uploadId.value}"
              )
              Left(NotFoundError)
            case status    =>
              logError(
                s"[SubmissionDetailsConnector][getSubmissionDetailsByUploadId] Unexpected response: status $status, uploadId ${uploadId.value}"
              )
              Left(InternalServerError)
          }
        }
    }
  }

  def getSubmissionDetailsByCarfId(
      carfId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): ResultT[Seq[SubmissionDetails]] = {
    val requestUrl = url"${config.carfReportingBaseUrl}/user-submission-details/$carfId"

    logInfo(
      s"[SubmissionDetailsConnector][getSubmissionDetailsByCarfId] Getting submission details for carfId $carfId"
    )

    ResultT.fromFuture {
      httpClient
        .get(requestUrl)
        .execute[HttpResponse]
        .map { response =>
          response.status match {
            case OK     =>
              Try(response.json.as[Seq[SubmissionDetails]]) match {
                case Success(fileDetailsList) => Right(fileDetailsList)
                case Failure(_)               =>
                  logWarn(
                    s"[SubmissionDetailsConnector][getSubmissionDetailsByCarfId] Error parsing response body for carfId $carfId"
                  )
                  Left(JsonValidationError)
              }
            case status =>
              logError(
                s"[SubmissionDetailsConnector][getSubmissionDetailsByCarfId] Unexpected response: status $status, carfId $carfId"
              )
              Left(InternalServerError)
          }
        }
    }
  }

}
