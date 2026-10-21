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
import play.api.i18n.Messages

class ReportTypeSpec extends SpecBase {

  implicit val messages: Messages = messages(app)

  "ReportType" - {
    ".fileInformationMessageForReportType" - {
      "must return the correct message key for each ReportType value" in {
        fileInformationMessageForReportType(TestData) mustBe "Test data"

        fileInformationMessageForReportType(NilReport) mustBe "No reportable information"

        fileInformationMessageForReportType(NotificationOfReportingOutsideUk) mustBe
          "Notification that this RCASP elects to report in another country outside the UK"

        fileInformationMessageForReportType(NewInformation) mustBe "New information"

        fileInformationMessageForReportType(AdditionalInformationForExistingReport) mustBe
          "Additional information for an existing report"

        fileInformationMessageForReportType(
          DeletionOfExistingReport
        ) mustBe "Deletion of the whole of an existing report"

        fileInformationMessageForReportType(CorrectedInformationForExistingReport) mustBe
          "Corrected information for an existing report"

        fileInformationMessageForReportType(DeletedInformationForExistingReport) mustBe
          "Deletion of selected information from an existing report"

        fileInformationMessageForReportType(CorrectedAndDeletedInformationForExistingReport) mustBe
          "Corrected and deleted information for an existing report"

        fileInformationMessageForReportType(ReportableInformationFallback) mustBe "Reportable information"
      }
    }

    ".warningMessageForReportType" - {
      "must return the correct message key if the ReportType requires a warning message, or None otherwise" in {
        warningMessageForReportType(TestData) mustBe
          Some("We cannot complete all checks on test data or accept the file.")

        warningMessageForReportType(NilReport) mustBe None

        warningMessageForReportType(NotificationOfReportingOutsideUk) mustBe
          Some(
            "Sending this file will tell us that this RCASP is electing to report in another country outside the UK. You will still need to send HMRC a report of their UK tax-resident cryptoasset users if there are any."
          )

        warningMessageForReportType(NewInformation) mustBe None

        warningMessageForReportType(AdditionalInformationForExistingReport) mustBe None

        warningMessageForReportType(DeletionOfExistingReport) mustBe
          Some("This will permanently delete the whole of an existing report.")

        warningMessageForReportType(CorrectedInformationForExistingReport) mustBe
          Some("This will permanently change reported information marked as a correction.")

        warningMessageForReportType(DeletedInformationForExistingReport) mustBe
          Some("This will permanently delete some of the information in an existing report.")

        warningMessageForReportType(CorrectedAndDeletedInformationForExistingReport) mustBe
          Some(
            "This will permanently change reported information marked as a correction. It will also permanently delete reported information marked for deletion."
          )

        warningMessageForReportType(ReportableInformationFallback) mustBe None
      }
    }
  }
}
