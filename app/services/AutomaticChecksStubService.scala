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

package services

import models.fileSubmission.{ResultOfAutomaticChecksStubData, SubmittedFileCheck}

import javax.inject.Singleton

@Singleton
class AutomaticChecksStubService {

  // TODO: replace with real lookups from the submissions store once CADX integration exists.
  def getSubmissions(carfId: String): Option[Seq[SubmittedFileCheck]] = carfId.headOption.map(_.toUpper) match {
    case Some('Z') => None
    case Some('X') => Some(Seq(ResultOfAutomaticChecksStubData.pending))
    case Some('W') => Some(Seq(ResultOfAutomaticChecksStubData.unexpectedError))
    case Some('V') => Some(Seq(ResultOfAutomaticChecksStubData.failedRules))
    case _         => Some(ResultOfAutomaticChecksStubData.allStatuses)
  }
}
