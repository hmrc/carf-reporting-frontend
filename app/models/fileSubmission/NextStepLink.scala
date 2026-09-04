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

sealed trait NextStepLink

object NextStepLink {
  case object GotoConfirmation extends NextStepLink
  case object CheckErrors extends NextStepLink
  case object UploadFileAgain extends NextStepLink
  case object ContactUs extends NextStepLink
  case object VirusFound extends NextStepLink
  case object NoLink extends NextStepLink

  def fromFileStatus(fileStatus: FileStatus): NextStepLink = fileStatus match {
    case FileStatus.Pending                => NoLink
    case FileStatus.Passed                 => GotoConfirmation
    case FileStatus.Failed                 => CheckErrors
    case FileStatus.VirusFound             => VirusFound
    case FileStatus.UnprocessableErrorFile => UploadFileAgain
    case FileStatus.UnexpectedError        => ContactUs
  }
}
