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

package models.requests.sdes

import models.ExtractedFileDetails
import models.responses.{DisplaySubscriptionDetails, RcaspDetails}
import models.upscan.UploadId
import play.api.libs.json.{Format, Json, OFormat}

case class SubmissionRequest(
    fileName: FileName,
    uploadId: UploadId,
    fileSize: Long,
    documentUrl: String,
    checksum: String,
    rcaspDetails: RcaspDetails,
    subscriptionDetails: DisplaySubscriptionDetails,
    extractedFileDetails: ExtractedFileDetails
)

object SubmissionRequest {
  implicit val format: OFormat[SubmissionRequest] = Json.format[SubmissionRequest]
}

case class FileName(value: String) extends AnyVal

object FileName {
  implicit val fileNameFormat: Format[FileName] = Json.valueFormat[FileName]
}
