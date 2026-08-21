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
import models.fileSubmission.FileStatus.{Failed, Passed}

class FileCheckResultHelperSpec extends SpecBase {

  private val helper = new FileCheckResultHelper()

  "FileCheckResultHelper" - {

    ".summaryList" - {

      "must build the correct rows when file status is Passed" in {
        implicit val msgs = messages(app)

        val result = helper.summaryList(
          messageRefId = testMessageRefId,
          fileStatus = Passed,
          messagePrefix = "filePassedChecks"
        )

        result.rows.size mustEqual 2

        val messageRefIdRow = result.rows.head
        messageRefIdRow.key.content.asHtml.toString   must include(
          msgs("filePassedChecks.summary.messageRefId.label")
        )
        messageRefIdRow.value.content.asHtml.toString must include(testMessageRefId)

        val resultRow = result.rows(1)
        resultRow.key.content.asHtml.toString   must include(
          msgs("filePassedChecks.summary.result.label")
        )
        resultRow.value.content.asHtml.toString must include("govuk-tag--green")
        resultRow.value.content.asHtml.toString must include(msgs("fileStatus.passed"))
      }

      "must build the correct rows when file status is Failed" in {
        implicit val msgs = messages(app)

        val result = helper.summaryList(
          messageRefId = testMessageRefId,
          fileStatus = Failed,
          messagePrefix = "fileFailedChecks"
        )

        result.rows.size mustEqual 2

        val messageRefIdRow = result.rows.head
        messageRefIdRow.key.content.asHtml.toString   must include(
          msgs("fileFailedChecks.summary.messageRefId.label")
        )
        messageRefIdRow.value.content.asHtml.toString must include(testMessageRefId)

        val resultRow = result.rows(1)
        resultRow.key.content.asHtml.toString   must include(
          msgs("fileFailedChecks.summary.result.label")
        )
        resultRow.value.content.asHtml.toString must include("govuk-tag--red")
        resultRow.value.content.asHtml.toString must include(msgs("fileStatus.failed"))
      }
    }
  }
}
