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

import models.{ReportType, UserAnswers}
import pages.ExtractedFileDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.all.{SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*

class FileConfirmationHelper {

  def rows(answers: UserAnswers, rcaspNameFromManagement: String)(implicit
      messages: Messages
  ): Option[Seq[SummaryListRow]] =
    answers.get(ExtractedFileDetailsPage).map { extractedFileDetails =>
      val messageRefId    = extractedFileDetails.messageRefId
      val sendingEntityIn = extractedFileDetails.sendingEntityIn
      val rcaspNameMaybe  = extractedFileDetails.rcaspName

      Seq(
        SummaryListRowViewModel(
          key = "fileConfirmation.fileId.label",
          value = ValueViewModel(HtmlContent(messageRefId)),
          actions = Seq.empty
        ),
        SummaryListRowViewModel(
          key = "fileConfirmation.rcaspId.label",
          value = ValueViewModel(HtmlContent(sendingEntityIn)),
          actions = Seq.empty
        ),
        rcaspNameMaybe.fold {
          SummaryListRowViewModel(
            key = "fileConfirmation.rcaspName.label.none",
            value = ValueViewModel(HtmlContent(rcaspNameFromManagement)),
            actions = Seq.empty
          )
        } { rcaspName =>
          SummaryListRowViewModel(
            key = "fileConfirmation.rcaspName.label",
            value = ValueViewModel(HtmlContent(rcaspName)),
            actions = Seq.empty
          )
        },
        SummaryListRowViewModel(
          key = "fileConfirmation.fileInformation.label",
          value = ValueViewModel(
            HtmlContent(
              ReportType.fileInformationMessageForReportType(extractedFileDetails.getReportType)
            )
          ),
          actions = Seq.empty
        )
      )
    }
}
