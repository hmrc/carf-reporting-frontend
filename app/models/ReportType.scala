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

import play.api.i18n.Messages

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

  def fileInformationMessageForReportType(reportType: ReportType)(implicit messages: Messages): String =
    reportType match {
      case TestData                                        => messages("reportType.testData")
      case NilReport                                       => messages("reportType.nilReport")
      case NotificationOfReportingOutsideUk                => messages("reportType.notificationOfReportingOutsideUk")
      case NewInformation                                  => messages("reportType.newInformation")
      case AdditionalInformationForExistingReport          => messages("reportType.additionalInformationForExistingReport")
      case DeletionOfExistingReport                        => messages("reportType.deletionOfExistingReport")
      case CorrectedInformationForExistingReport           => messages("reportType.correctedInformationForExistingReport")
      case DeletedInformationForExistingReport             => messages("reportType.deletedInformationForExistingReport")
      case CorrectedAndDeletedInformationForExistingReport =>
        messages("reportType.correctedAndDeletedInformationForExistingReport")
      case ReportableInformationFallback                   => messages("reportType.reportableInformation")
    }

  def warningMessageForReportType(
      reportType: ReportType,
      rcaspName: String
  )(implicit messages: Messages): Option[String] =
    reportType match {
      case TestData                                        => Some(messages("reportType.testData.warning"))
      case NotificationOfReportingOutsideUk                =>
        Some(messages("reportType.notificationOfReportingOutsideUk.warning", rcaspName))
      case DeletionOfExistingReport                        => Some(messages("reportType.deletionOfExistingReport.warning"))
      case CorrectedInformationForExistingReport           =>
        Some(messages("reportType.correctedInformationForExistingReport.warning"))
      case DeletedInformationForExistingReport             =>
        Some(messages("reportType.deletedInformationForExistingReport.warning"))
      case CorrectedAndDeletedInformationForExistingReport =>
        Some(messages("reportType.correctedAndDeletedInformationForExistingReport.warning"))
      case _                                               => None
    }
}
