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
import models.errors.ApiError.{InternalServerError, JsonValidationError}
import models.responses.{RcaspDetails, ViewRcaspResponse}
import play.api.http.Status.{NOT_FOUND, OK}
import types.ResultT
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.LoggerUtil.*

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class RcaspRegistrationConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2) {

  def viewRcasps(
      carfId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): ResultT[List[RcaspDetails]] = {
    val viewRcaspUrl = url"${config.carfRegistrationBaseUrl}/carf-management/view-rcasp/$carfId/none"

    logInfo(s"[RcaspRegistrationConnector][viewRcasps] Calling endpoint: ${viewRcaspUrl.toString}")

    ResultT.fromFuture {
      http
        .get(viewRcaspUrl)
        .execute[HttpResponse]
        .map { response =>
          response.status match {
            case OK        =>
              Try(response.json.as[ViewRcaspResponse]) match {
                case Success(viewRcaspResponse) =>
                  Right(viewRcaspResponse.ViewRCASP.ResponseDetails.RCASPList)
                case Failure(_)                 =>
                  logWarn(
                    s"[RcaspRegistrationConnector][viewRcasps] Error parsing ViewRcaspResponse from $viewRcaspUrl"
                  )
                  Left(JsonValidationError)
              }
            case NOT_FOUND =>
              logInfo(s"[RcaspRegistrationConnector][viewRcasps] No RCASPs found for carfId: $carfId")
              Right(List.empty)
            case status    =>
              logWarn(
                s"[RcaspRegistrationConnector][viewRcasps] Unexpected response: status $status from $viewRcaspUrl"
              )
              Left(InternalServerError)
          }
        }
    }
  }
}
