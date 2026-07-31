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
import models.DocTypeIndic.OECD11
import models.ExtractedFileDetails
import models.MessageTypeIndic.{CARF701, CARF703}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class CheckYourFileDetailsHelperSpec extends SpecBase {

  val helper = new CheckYourFileDetailsHelper

  implicit val messages: Messages = messages(app)

  "CheckYourFileDetailsHelper" - {
    ".fileDetailsSummaryList" - {
      "must return a summary list including RCASP name row when rcaspName is present in ExtractedFileDetails (not NilReport)" in {
        val extractedFileDetails = ExtractedFileDetails(
          messageRefId = testMessageRefId,
          sendingEntityIn = testRcaspId,
          rcaspName = Some(testRcaspName),
          messageTypeIndic = CARF701,
          hasOtherNexus = false,
          hasCryptoUsers = true,
          docTypeIndic = OECD11,
          isTestData = false,
          allCryptoUsersAreCorrections = false,
          allCryptoUsersAreDeletions = false
        )

        val expectedKeys = List(
          Text("File ID (MessageRefId)"),
          Text("RCASP ID (SendingEntityIN)"),
          Text("Reporting cryptoasset service provider (RCASP Name)"),
          Text("File information")
        )

        val expectedValues = List(
          Text(testMessageRefId),
          Text(testRcaspId),
          Text(testRcaspName),
          Text("New information")
        )

        val expectedClasses = List(
          "govuk-summary-list__row--no-actions",
          "govuk-summary-list__row--no-actions",
          "govuk-summary-list__row--no-actions",
          ""
        )

        val expectedActions = List(
          None,
          None,
          None,
          Some(
            Seq(
              ActionItem(
                href = controllers.upload.routes.UploadXmlController.onPageLoad().url,
                content = Text("Change file")
              )
            )
          )
        )

        val summaryList = helper.fileDetailsSummaryList(extractedFileDetails)

        summaryList.rows.map(_.key.content)          mustBe expectedKeys
        summaryList.rows.map(_.value.content)        mustBe expectedValues
        summaryList.rows.map(_.classes)              mustBe expectedClasses
        summaryList.rows.map(_.actions.map(_.items)) mustBe expectedActions
      }

      "must return a summary list excluding RCASP name row when rcaspName is None in ExtractedFileDetails (NilReport)" in {
        val extractedFileDetails = ExtractedFileDetails(
          messageRefId = testMessageRefId,
          sendingEntityIn = testRcaspId,
          rcaspName = None,
          messageTypeIndic = CARF703,
          hasOtherNexus = false,
          hasCryptoUsers = false,
          docTypeIndic = OECD11,
          isTestData = false,
          allCryptoUsersAreCorrections = false,
          allCryptoUsersAreDeletions = false
        )

        val expectedKeys = List(
          Text("File ID (MessageRefId)"),
          Text("RCASP ID (SendingEntityIN)"),
          Text("File information")
        )

        val expectedValues = List(
          Text(testMessageRefId),
          Text(testRcaspId),
          Text("No reportable information")
        )

        val expectedClasses = List(
          "govuk-summary-list__row--no-actions",
          "govuk-summary-list__row--no-actions",
          ""
        )

        val expectedActions = List(
          None,
          None,
          Some(
            Seq(
              ActionItem(
                href = controllers.upload.routes.UploadXmlController.onPageLoad().url,
                content = Text("Change file")
              )
            )
          )
        )

        val summaryList = helper.fileDetailsSummaryList(extractedFileDetails)

        summaryList.rows.map(_.key.content)          mustBe expectedKeys
        summaryList.rows.map(_.value.content)        mustBe expectedValues
        summaryList.rows.map(_.classes)              mustBe expectedClasses
        summaryList.rows.map(_.actions.map(_.items)) mustBe expectedActions
      }
    }
  }
}
