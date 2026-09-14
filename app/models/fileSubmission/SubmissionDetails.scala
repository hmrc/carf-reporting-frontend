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

package models.fileSubmission

import models.ExtractedFileDetails
import models.errors.BusinessRuleValidationErrors
import models.responses.{DisplaySubscriptionDetails, RcaspDetails}
import models.upscan.UploadId
import play.api.libs.json.*
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import utils.DateTimeFormats

import java.time.{Instant, ZoneOffset}

case class SubmissionDetails(
    _id: UploadId,
    carfId: String,
    fileStatus: FileStatus,
    fileName: String,
    extractedFileDetails: ExtractedFileDetails,
    rcaspDetails: RcaspDetails,
    subscriptionDetails: DisplaySubscriptionDetails,
    submissionTime: Instant,
    lastStatusUpdateTime: Instant,
    businessRuleErrors: BusinessRuleValidationErrors
) {
  def formattedSubmissionTime: String = {
    val dateTime = submissionTime.atZone(ZoneOffset.UTC).toLocalDateTime
    DateTimeFormats.dateTimeToStringWithoutAt(dateTime)
  }
}

object SubmissionDetails {

  import play.api.libs.functional.syntax.*

  implicit val reads: Reads[SubmissionDetails] =
    (
      (__ \ "_id").read[UploadId] and
        (__ \ "carfId").read[String] and
        (__ \ "fileStatus").read[FileStatus] and
        (__ \ "fileName").read[String] and
        (__ \ "extractedFileDetails").read[ExtractedFileDetails] and
        (__ \ "rcaspDetails").read[RcaspDetails] and
        (__ \ "subscriptionDetails").read[DisplaySubscriptionDetails] and
        (__ \ "submissionTime").read(MongoJavatimeFormats.instantFormat) and
        (__ \ "lastStatusUpdateTime").read(MongoJavatimeFormats.instantFormat) and
        (__ \ "businessRuleErrors").read[BusinessRuleValidationErrors]
    )(SubmissionDetails.apply _)

  implicit val writes: OWrites[SubmissionDetails] =
    (
      (__ \ "_id").write[UploadId] and
        (__ \ "carfId").write[String] and
        (__ \ "fileStatus").write[FileStatus] and
        (__ \ "fileName").write[String] and
        (__ \ "extractedFileDetails").write[ExtractedFileDetails] and
        (__ \ "rcaspDetails").write[RcaspDetails] and
        (__ \ "subscriptionDetails").write[DisplaySubscriptionDetails] and
        (__ \ "submissionTime").write(MongoJavatimeFormats.instantFormat) and
        (__ \ "lastStatusUpdateTime").write(MongoJavatimeFormats.instantFormat) and
        (__ \ "businessRuleErrors").write[BusinessRuleValidationErrors]
      )(o => Tuple.fromProductTyped(o))

  implicit val format: OFormat[SubmissionDetails] = OFormat(reads, writes)

}
