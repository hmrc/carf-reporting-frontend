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

package models

import models.ReportType.*
import play.api.libs.json.*

enum ReportType {
  case TestData
  case NilReport
  case NotificationOfReportingOutsideUk
  case NewInformation
  case AdditionalInformationForExistingReport
  case DeletionOfExistingReport
  case CorrectedInformationForExistingReport
  case DeletedInformationForExistingReport
  case CorrectedAndDeletedInformationForExistingReport
  case ReportableInformationFallback
}

object ReportType {
  private val fromJson: Map[String, ReportType] = Map(
    "TEST_DATA"                                             -> TestData,
    "NIL_REPORT"                                            -> NilReport,
    "NOTIFICATION_OF_REPORTING_OUTSIDE_UK"                  -> NotificationOfReportingOutsideUk,
    "NEW_INFORMATION"                                       -> NewInformation,
    "ADDITIONAL_INFORMATION_FOR_EXISTING_REPORT"            -> AdditionalInformationForExistingReport,
    "DELETION_OF_EXISTING_REPORT"                           -> DeletionOfExistingReport,
    "CORRECTED_INFORMATION_FOR_EXISTING_REPORT"             -> CorrectedInformationForExistingReport,
    "DELETED_INFORMATION_FOR_EXISTING_REPORT"               -> DeletedInformationForExistingReport,
    "CORRECTED_AND_DELETED_INFORMATION_FOR_EXISTING_REPORT" -> CorrectedAndDeletedInformationForExistingReport,
    "REPORTABLE_INFORMATION"                                -> ReportableInformationFallback
  )

  private val toJson: Map[ReportType, String] = fromJson.map(_.swap)

  given Format[ReportType] = new Format[ReportType] {

    override def reads(json: JsValue): JsResult[ReportType] =
      json match {
        case JsString(value) =>
          fromJson
            .get(value)
            .map(JsSuccess(_))
            .getOrElse(JsError(s"Invalid ReportType value: $value"))
        case _               =>
          JsError("ReportType must be a string")
      }

    override def writes(value: ReportType): JsValue =
      JsString(toJson(value))
  }

  def fileInformationMessageKeyForReportType(reportType: ReportType): String =
    reportType match {
      case TestData                                        => "reportType.testData"
      case NilReport                                       => "reportType.nilReport"
      case NotificationOfReportingOutsideUk                => "reportType.notificationOfReportingOutsideUk"
      case NewInformation                                  => "reportType.newInformation"
      case AdditionalInformationForExistingReport          => "reportType.additionalInformationForExistingReport"
      case DeletionOfExistingReport                        => "reportType.deletionOfExistingReport"
      case CorrectedInformationForExistingReport           => "reportType.correctedInformationForExistingReport"
      case DeletedInformationForExistingReport             => "reportType.deletedInformationForExistingReport"
      case CorrectedAndDeletedInformationForExistingReport =>
        "reportType.correctedAndDeletedInformationForExistingReport"
      case ReportableInformationFallback                   => "reportType.reportableInformation"
    }

  def warningMessageKeyForReportType(reportType: ReportType): Option[String] =
    reportType match {
      case TestData                                        => Some("reportType.testData.warning")
      case NotificationOfReportingOutsideUk                => Some("reportType.notificationOfReportingOutsideUk.warning")
      case DeletionOfExistingReport                        => Some("reportType.deletionOfExistingReport.warning")
      case CorrectedInformationForExistingReport           => Some("reportType.correctedInformationForExistingReport.warning")
      case DeletedInformationForExistingReport             => Some("reportType.deletedInformationForExistingReport.warning")
      case CorrectedAndDeletedInformationForExistingReport =>
        Some("reportType.correctedAndDeletedInformationForExistingReport.warning")
      case _                                               => None
    }
}
