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

import play.api.i18n.Messages
import play.api.libs.json.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import viewmodels.govuk.all.{FluentTag, TagViewModel}

enum FileStatus {
  case Pending
  case Passed
  case Failed
  case VirusFound
  case UnprocessableErrorFile
  case UnexpectedError
}

object FileStatus {

  given Format[FileStatus] = Format(
    Reads {
      case JsString("Pending")                => JsSuccess(Pending)
      case JsString("Passed")                 => JsSuccess(Passed)
      case JsString("Failed")                 => JsSuccess(Failed)
      case JsString("VirusFound")             => JsSuccess(VirusFound)
      case JsString("UnprocessableErrorFile") => JsSuccess(UnprocessableErrorFile)
      case JsString("UnexpectedError")        => JsSuccess(UnexpectedError)
      case other                              => JsError(s"Invalid FileStatus JSON: $other")
    },
    Writes {
      case Pending                => JsString("Pending")
      case Passed                 => JsString("Passed")
      case Failed                 => JsString("Failed")
      case VirusFound             => JsString("VirusFound")
      case UnprocessableErrorFile => JsString("UnprocessableErrorFile")
      case UnexpectedError        => JsString("UnexpectedError")
    }
  )

  def tagForFileStatus(fileStatus: FileStatus)(implicit messages: Messages): Tag =
    fileStatus match {
      case Pending                                  => TagViewModel(Text(messages("fileStatus.pending"))).yellow()
      case Passed                                   => TagViewModel(Text(messages("fileStatus.passed"))).green()
      case Failed | VirusFound                      => TagViewModel(Text(messages("fileStatus.failed"))).red()
      case UnprocessableErrorFile | UnexpectedError =>
        TagViewModel(Text(messages("fileStatus.problem"))).purple()
    }
}
