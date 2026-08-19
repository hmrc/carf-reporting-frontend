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

package models.fileSubmission

import base.SpecBase
import models.fileSubmission.FileStatus.*
import play.api.i18n.Messages
import play.api.libs.json.{JsError, Json}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.all.{FluentTag, TagViewModel}

class FileStatusSpec extends SpecBase {

  implicit val messages: Messages = messages(app)

  "FileStatus" - {
    "json reads" - {
      "must parse to expected file status" in {
        Json.parse("\"Pending\"").as[FileStatus]                mustBe Pending
        Json.parse("\"Passed\"").as[FileStatus]                 mustBe Passed
        Json.parse("\"Failed\"").as[FileStatus]                 mustBe Failed
        Json.parse("\"VirusFound\"").as[FileStatus]             mustBe VirusFound
        Json.parse("\"UnprocessableErrorFile\"").as[FileStatus] mustBe UnprocessableErrorFile
        Json.parse("\"UnexpectedError\"").as[FileStatus]        mustBe UnexpectedError
      }

      "must return a JsError when parsing an unexpected value" in {
        Json.parse("\"Unknown\"").validate[FileStatus] mustBe JsError("""Invalid FileStatus JSON: "Unknown"""")
      }
    }

    "json writes" - {
      "must write to json as expected" in {
        Json.toJson(Pending).toString                mustBe "\"Pending\""
        Json.toJson(Passed).toString                 mustBe "\"Passed\""
        Json.toJson(Failed).toString                 mustBe "\"Failed\""
        Json.toJson(VirusFound).toString             mustBe "\"VirusFound\""
        Json.toJson(UnprocessableErrorFile).toString mustBe "\"UnprocessableErrorFile\""
        Json.toJson(UnexpectedError).toString        mustBe "\"UnexpectedError\""
      }
    }

    ".tagForFileStatus" - {
      "must return the correct tag for each file status" in {
        tagForFileStatus(Pending)                mustBe TagViewModel(Text("Pending")).yellow()
        tagForFileStatus(Passed)                 mustBe TagViewModel(Text("Passed")).green()
        tagForFileStatus(Failed)                 mustBe TagViewModel(Text("Failed")).red()
        tagForFileStatus(VirusFound)             mustBe TagViewModel(Text("Failed")).red()
        tagForFileStatus(UnprocessableErrorFile) mustBe TagViewModel(Text("Problem")).purple()
        tagForFileStatus(UnexpectedError)        mustBe TagViewModel(Text("Problem")).purple()
      }
    }
  }
}
