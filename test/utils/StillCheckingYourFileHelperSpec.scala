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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class StillCheckingYourFileHelperSpec extends SpecBase {

  val helper = new StillCheckingYourFileHelper

  implicit val messages: Messages = messages(app)

  "StillCheckingYourFileHelper" - {
    ".stillCheckingYourFileSummaryList" - {
      "must return a summary list with messageRefId and pending tag" in {
        val expectedKeys = List(
          Text("File ID (MessageRefId)"),
          Text("Result of automatic checks")
        )

        val expectedValues = List(
          Text(testMessageRefId),
          HtmlContent(
            """<strong class="govuk-tag  govuk-tag--yellow">
              |  Pending
              |</strong>
              |""".stripMargin
          )
        )

        val summaryList = helper.stillCheckingYourFileSummaryList(testMessageRefId)

        summaryList.rows.map(_.key.content)   mustBe expectedKeys
        summaryList.rows.map(_.value.content) mustBe expectedValues
        summaryList.rows.flatMap(_.actions)   mustBe Seq.empty
      }
    }
  }
}
