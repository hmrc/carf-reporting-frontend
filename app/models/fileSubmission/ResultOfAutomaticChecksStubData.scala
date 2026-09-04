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

import java.time.LocalDateTime

object ResultOfAutomaticChecksStubData {

  private val abcBank: String   = "ABC Bank plc"
  private val myCompany: String = "My Company Ltd"

  val passed: SubmittedFileCheck = SubmittedFileCheck(
    abcBank,
    "GB2026GB-CAR1234567892_Valid",
    LocalDateTime.now(),
    FileStatus.Passed
  )

  val failedRules: SubmittedFileCheck = SubmittedFileCheck(
    abcBank,
    "GB2026GB-CAR1234567892_BR-invalid",
    LocalDateTime.now().minusMinutes(5),
    FileStatus.Failed
  )

  val pending: SubmittedFileCheck = SubmittedFileCheck(
    abcBank,
    "GB2026GB-CAR1234567892_CADX-down",
    LocalDateTime.now().minusMinutes(10),
    FileStatus.Pending
  )

  val unexpectedError: SubmittedFileCheck = SubmittedFileCheck(
    abcBank,
    "GB2026GB-CAR1234567892_CADX-error",
    LocalDateTime.of(2023, 7, 24, 9, 0),
    FileStatus.UnexpectedError
  )

  val virusFound: SubmittedFileCheck = SubmittedFileCheck(
    myCompany,
    "GB2026GB-CAR1234567891_SDES-virus",
    LocalDateTime.of(2023, 7, 23, 13, 0),
    FileStatus.VirusFound
  )

  val unprocessableErrorFile: SubmittedFileCheck = SubmittedFileCheck(
    myCompany,
    "GB2026GB-CAR1234567891_SDES-error",
    LocalDateTime.of(2023, 7, 22, 15, 10),
    FileStatus.UnprocessableErrorFile
  )

  // One row per status type, most recent first, matching prototype ordering
  val allStatuses: Seq[SubmittedFileCheck] =
    Seq(passed, failedRules, pending, unexpectedError, virusFound, unprocessableErrorFile)
}
