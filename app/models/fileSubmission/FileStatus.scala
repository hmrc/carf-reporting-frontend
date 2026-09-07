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
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import viewmodels.govuk.all.{FluentTag, TagViewModel}
import views.html.components.Link

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

  def linkForFileStatus(fileStatus: FileStatus)(implicit messages: Messages): HtmlContent =
    fileStatus match {
      case Pending                =>
        HtmlContent(s"<span class='govuk-visually-hidden'>${messages("resultOfAutomaticChecks.nextStep.none")}</span>")
      case Passed                 =>
        HtmlContent(
          Link()(
            href = controllers.routes.PlaceholderController.onPageLoad("TODO: file-confirmation page").url,
            key = "resultOfAutomaticChecks.nextStep.confirmation"
          )
        )
      case Failed                 =>
        HtmlContent(
          Link()(
            href = controllers.problem.routes.RulesErrorsController.onPageLoad().url,
            key = "resultOfAutomaticChecks.nextStep.checkErrors"
          )
        )
      case VirusFound             =>
        HtmlContent(
          Link()(
            href = controllers.problem.routes.VirusFoundController.onPageLoad().url,
            key = "resultOfAutomaticChecks.nextStep.checkProblem"
          )
        )
      case UnprocessableErrorFile =>
        HtmlContent(
          Link()(
            href = controllers.upload.routes.UploadXmlController.onPageLoad().url,
            key = "resultOfAutomaticChecks.nextStep.uploadAgain"
          )
        )
      case UnexpectedError        =>
        HtmlContent(
          Link()(
            href = controllers.routes.PlaceholderController
              .onPageLoad("Should redirect to /problem/file-not-accepted (ticket TBC)")
              .url,
            key = "resultOfAutomaticChecks.nextStep.contactUs"
          )
        )
    }
}
