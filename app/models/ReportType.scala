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
