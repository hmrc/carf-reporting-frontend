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

import config.Constants.bytesInMb
import config.FrontendAppConfig
import models.errors.ApiError.{InternalServerError, JsonValidationError}
import models.upscan.*
import play.api.Logging
import play.api.http.HeaderNames
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import types.ResultT
import uk.gov.hmrc.http
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class UpscanConnector @Inject() (val config: FrontendAppConfig, val http: HttpClientV2) extends Logging {

  private val headers = Map(
    HeaderNames.CONTENT_TYPE -> "application/json"
  )

  def upscanFormInitiate(
      uploadId: UploadId
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): ResultT[UpscanInitiateResponse] = {
    val callbackUrl = s"$backendUrl/upscan/callback"
    val body        = UpscanInitiateRequest(
      callbackUrl,
      successRedirect =
        s"$upscanRedirectBase${controllers.upload.routes.UploadXmlController.getUploadStatusAndRedirect(uploadId).url}",
      errorRedirect = s"$upscanRedirectBase/send-a-cryptoasset-report/error",
      minimumFileSize = None,
      maximumFileSize = upscanMaxSizeInMb * bytesInMb
    )

    ResultT.fromFuture {
      http
        .post(url"$upscanInitiateUrl")
        .withBody(Json.toJson(body))
        .setHeader(headers.toSeq: _*)
        .execute[HttpResponse]
        .map { response =>
          response.status match {
            case OK =>
              Try(response.json.as[PreparedUpload]) match {
                case Success(preparedUpload) => Right(preparedUpload.toUpscanInitiateResponse)
                case Failure(exception)      =>
                  logger.warn("[UpscanConnector][upscanFormInitiate] Error parsing response body as PreparedUpload")
                  Left(JsonValidationError)
              }
            case _  =>
              logger.warn(s"[UpscanConnector][upscanFormInitiate] Unexpected response with status ${response.status}")
              Left(InternalServerError)
          }
        }
    }
  }

  def saveRequestedUpload(
      uploadId: UploadId,
      fileReference: Reference
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): ResultT[Unit] = {
    val uploadUrl = url"$backendUrl/upscan/upload"

    ResultT.fromFuture {
      http
        .post(uploadUrl)
        .withBody(Json.toJson(UpscanIdentifiers(uploadId, fileReference)))
        .execute[HttpResponse]
        .map { response =>
          response.status match {
            case OK => Right(())
            case _  =>
              logger.warn(s"[UpscanConnector][saveRequestedUpload] Unexpected response with status ${response.status}")
              Left(InternalServerError)
          }
        }
    }
  }

  def getUploadStatus(
      uploadId: UploadId
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): ResultT[Option[UploadStatus]] = {
    val statusUrl = url"$backendUrl/upscan/status/${uploadId.value}"

    ResultT.fromFuture {
      http.get(statusUrl).execute[HttpResponse].map { response =>
        response.status match {
          case OK        =>
            Try(response.json.as[UploadStatus]) match {
              case Success(status)    => Right(Some(status))
              case Failure(exception) =>
                logger.warn("[UpscanConnector][getUploadStatus] Error parsing response body as UploadStatus")
                Left(JsonValidationError)
            }
          case NOT_FOUND =>
            logger.warn(s"[UpscanConnector][getUploadStatus] No record found for uploadId ${uploadId.value}")
            Right(None)
          case _         =>
            logger.warn(s"[UpscanConnector][getUploadStatus] Unexpected response with status ${response.status}")
            Left(InternalServerError)
        }
      }
    }
  }

  private lazy val backendUrl         = config.carfReportingBaseUrl
  private lazy val upscanInitiateUrl  = s"${config.upscanInitiateHost}${config.upscanInitiatePath}"
  private lazy val upscanRedirectBase = config.upscanRedirectBase
  private lazy val upscanMaxSizeInMb  = config.upscanMaxFileSizeInMb
}
