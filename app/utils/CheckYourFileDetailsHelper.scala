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

import models.{ExtractedFileDetails, ReportType}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, SummaryList, SummaryListRow}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.all.{ActionItemViewModel, FluentKey, FluentSummaryList, FluentValue, KeyViewModel, SummaryListViewModel, ValueViewModel}

class CheckYourFileDetailsHelper {

  def fileDetailsSummaryList(extractedFileDetails: ExtractedFileDetails)(implicit messages: Messages): SummaryList = {
    val fileIdRow = createSummaryListRow(
      messages("checkYourFileDetails.fileId.key"),
      extractedFileDetails.messageRefId,
      classes = "govuk-summary-list__row--no-actions"
    )

    val rcaspIdRow = createSummaryListRow(
      messages("checkYourFileDetails.rcaspId.key"),
      extractedFileDetails.sendingEntityIn,
      classes = "govuk-summary-list__row--no-actions"
    )

    val fileInformationRow = createSummaryListRow(
      messages("checkYourFileDetails.fileInformation.key"),
      ReportType.fileInformationMessageForReportType(extractedFileDetails.getReportType),
      maybeActionItem = Some(
        ActionItemViewModel(
          href = controllers.upload.routes.UploadXmlController.onPageLoad().url,
          content = Text(messages("checkYourFileDetails.fileInformation.change"))
        )
      )
    )

    val rows = extractedFileDetails.rcaspName.fold(
      Seq(fileIdRow, rcaspIdRow, fileInformationRow)
    ) { rcaspName =>
      val rcaspNameRow = createSummaryListRow(
        messages("checkYourFileDetails.rcaspName.key"),
        rcaspName,
        classes = "govuk-summary-list__row--no-actions"
      )
      Seq(fileIdRow, rcaspIdRow, rcaspNameRow, fileInformationRow)
    }

    SummaryListViewModel(rows = rows).withCssClass("govuk-!-margin-bottom-0").withoutBorders()
  }

  private def createSummaryListRow(
      key: String,
      value: String,
      classes: String = "",
      maybeActionItem: Option[ActionItem] = None
  ) =
    SummaryListRow(
      key = KeyViewModel(content = Text(key)).withCssClass("govuk-summary-check-details__key"),
      value = ValueViewModel(content = Text(value)).withCssClass("govuk-summary-check-details__value"),
      classes = classes,
      actions = maybeActionItem.map(actionItem => Actions(items = Seq(actionItem)))
    )
}
