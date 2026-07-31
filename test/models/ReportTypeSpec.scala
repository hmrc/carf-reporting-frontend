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

import base.SpecBase
import models.ReportType.*

class ReportTypeSpec extends SpecBase {

  "ReportType" - {
    ".fileInformationMessageKeyForReportType" - {
      "must return the correct message key for each ReportType value" in {
        fileInformationMessageKeyForReportType(TestData) mustBe "reportType.testData"

        fileInformationMessageKeyForReportType(NilReport) mustBe "reportType.nilReport"

        fileInformationMessageKeyForReportType(NotificationOfReportingOutsideUk) mustBe
          "reportType.notificationOfReportingOutsideUk"

        fileInformationMessageKeyForReportType(NewInformation) mustBe "reportType.newInformation"

        fileInformationMessageKeyForReportType(AdditionalInformationForExistingReport) mustBe
          "reportType.additionalInformationForExistingReport"

        fileInformationMessageKeyForReportType(DeletionOfExistingReport) mustBe "reportType.deletionOfExistingReport"

        fileInformationMessageKeyForReportType(CorrectedInformationForExistingReport) mustBe
          "reportType.correctedInformationForExistingReport"

        fileInformationMessageKeyForReportType(DeletedInformationForExistingReport) mustBe
          "reportType.deletedInformationForExistingReport"

        fileInformationMessageKeyForReportType(CorrectedAndDeletedInformationForExistingReport) mustBe
          "reportType.correctedAndDeletedInformationForExistingReport"

        fileInformationMessageKeyForReportType(ReportableInformationFallback) mustBe "reportType.reportableInformation"
      }
    }

    ".warningMessageKeyForReportType" - {
      "must return the correct message key if the ReportType requires a warning message, or None otherwise" in {
        warningMessageKeyForReportType(TestData) mustBe Some("reportType.testData.warning")

        warningMessageKeyForReportType(NilReport) mustBe None

        warningMessageKeyForReportType(NotificationOfReportingOutsideUk) mustBe
          Some("reportType.notificationOfReportingOutsideUk.warning")

        warningMessageKeyForReportType(NewInformation) mustBe None

        warningMessageKeyForReportType(AdditionalInformationForExistingReport) mustBe None

        warningMessageKeyForReportType(DeletionOfExistingReport) mustBe
          Some("reportType.deletionOfExistingReport.warning")

        warningMessageKeyForReportType(CorrectedInformationForExistingReport) mustBe
          Some("reportType.correctedInformationForExistingReport.warning")

        warningMessageKeyForReportType(DeletedInformationForExistingReport) mustBe
          Some("reportType.deletedInformationForExistingReport.warning")

        warningMessageKeyForReportType(CorrectedAndDeletedInformationForExistingReport) mustBe
          Some("reportType.correctedAndDeletedInformationForExistingReport.warning")

        warningMessageKeyForReportType(ReportableInformationFallback) mustBe None
      }
    }
  }
}
