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

package utils

import base.SpecBase
import models.{ExtractedFileDetails, ReportType}
import pages.ExtractedFileDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.govuk.all.{SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*

class FileConfirmationHelperSpec extends SpecBase {

  val helper = new FileConfirmationHelper()

  implicit val msgs: Messages = messages(app)

  "FileConfirmationHelper" - {
    ".rows" - {
      "must return summaryRows containing 4 resultingRows when ExtractedFileDetails is present and rcaspName is defined" in {
        val fileDetails = extractedFileDetailsTestData
        val userAnswers = emptyUserAnswers.withPage(ExtractedFileDetailsPage, extractedFileDetailsTestData)

        val resultingRows = helper.rows(userAnswers, organisationStandardRcaspDetails.RCASPName).get

        resultingRows.size mustEqual 4

        resultingRows.head mustEqual SummaryListRowViewModel(
          key = "fileConfirmation.fileId.label",
          value = ValueViewModel(HtmlContent(fileDetails.messageRefId)),
          actions = Seq.empty
        )

        resultingRows(1) mustEqual SummaryListRowViewModel(
          key = "fileConfirmation.rcaspId.label",
          value = ValueViewModel(HtmlContent(fileDetails.sendingEntityIn)),
          actions = Seq.empty
        )

        resultingRows(2) mustEqual SummaryListRowViewModel(
          key = "fileConfirmation.rcaspName.label",
          value = ValueViewModel(HtmlContent(fileDetails.rcaspName.get)),
          actions = Seq.empty
        )

        resultingRows(3) mustEqual SummaryListRowViewModel(
          key = "fileConfirmation.fileInformation.label",
          value = ValueViewModel(
            HtmlContent(ReportType.fileInformationMessageForReportType(fileDetails.getReportType))
          ),
          actions = Seq.empty
        )
      }

      "must return summaryRows containing 4 resultingRows when ExtractedFileDetails is present but rcaspName is missing" in {
        val fileDetailsWithoutName  = extractedFileDetailsTestData.copy(rcaspName = None)
        val rcaspNameFromManagement = organisationStandardRcaspDetails.RCASPName
        val userAnswers             = emptyUserAnswers.withPage(ExtractedFileDetailsPage, fileDetailsWithoutName)
        val resultingRows           = helper.rows(userAnswers, rcaspNameFromManagement).get

        resultingRows.size mustEqual 4

        resultingRows.head mustEqual SummaryListRowViewModel(
          key = "fileConfirmation.fileId.label",
          value = ValueViewModel(HtmlContent(fileDetailsWithoutName.messageRefId)),
          actions = Seq.empty
        )

        resultingRows(1) mustEqual SummaryListRowViewModel(
          key = "fileConfirmation.rcaspId.label",
          value = ValueViewModel(HtmlContent(fileDetailsWithoutName.sendingEntityIn)),
          actions = Seq.empty
        )

        resultingRows(2) mustEqual SummaryListRowViewModel(
          key = "fileConfirmation.rcaspName.label.none",
          value = ValueViewModel(HtmlContent(rcaspNameFromManagement)),
          actions = Seq.empty
        )

        resultingRows(3) mustEqual SummaryListRowViewModel(
          key = "fileConfirmation.fileInformation.label",
          value = ValueViewModel(
            HtmlContent(ReportType.fileInformationMessageForReportType(fileDetailsWithoutName.getReportType))
          ),
          actions = Seq.empty
        )
      }

      "must return None when ExtractedFileDetailsPage is missing from UserAnswers" in {
        val result = helper.rows(emptyUserAnswers, organisationStandardRcaspDetails.RCASPName)
        result mustBe None
      }
    }
  }
}
