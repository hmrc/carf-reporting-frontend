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
import models.fileSubmission.FileStatus.Passed
import models.responses.{getName, DisplaySubscriptionDetails, RcaspDetails}
import models.upscan.UploadId
import play.api.i18n.Messages
import play.api.libs.json.*
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import utils.DateTimeFormats

import java.time.{Instant, ZoneOffset}

sealed trait SubmissionRecord {
  val submissionTime: Instant

  def messageRefId: String
  def rcaspName: String

  def formattedSubmissionTime: String = {
    val dateTime = submissionTime.atZone(ZoneOffset.UTC).toLocalDateTime
    DateTimeFormats.dateTimeToStringWithoutAt(dateTime)
  }

  def resultTag(implicit messages: Messages): Tag

  def nextStepHtmlContent(implicit messages: Messages): HtmlContent
}

object SubmissionRecord {
  implicit val reads: Reads[SubmissionRecord] = Reads { json =>
    (json \ "carfId").validateOpt[String].flatMap {
      case Some(_) => json.validate[SubmissionDetails]
      case None    => json.validate[SubmissionHistoryPassed]
    }
  }
}

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
) extends SubmissionRecord {
  def messageRefId: String = extractedFileDetails.messageRefId
  def rcaspName: String    = rcaspDetails.getName

  def resultTag(implicit messages: Messages): Tag = FileStatus.tagForFileStatus(fileStatus)

  def nextStepHtmlContent(implicit messages: Messages): HtmlContent =
    FileStatus.nextStepForFileStatus(fileStatus, _id.value)
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

}

case class SubmissionHistoryPassed(
    messageRefId: String,
    rcaspName: String,
    submissionTime: Instant
) extends SubmissionRecord {
  def resultTag(implicit messages: Messages): Tag = FileStatus.tagForFileStatus(Passed)

  def nextStepHtmlContent(implicit messages: Messages): HtmlContent =
    HtmlContent(messages("detailsOfSentFiles.nextStep.confirmationNotAvailable"))
}

object SubmissionHistoryPassed {
  implicit val reads: Reads[SubmissionHistoryPassed] = Json.reads[SubmissionHistoryPassed]
}
