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

import models.UserAnswers
import models.problem.{BusinessRuleError, RulesErrorsStubData}
import pages.UploadSuccessDetailsPage

import javax.inject.Singleton

@Singleton
class RulesErrorsStubService {

  private val stubFileName: String = "filename.xml"

  def getFileName(carfId: String, userAnswers: Option[UserAnswers]): Option[String] =
    userAnswers.flatMap(_.get(UploadSuccessDetailsPage).map(_.fileName)).orElse {
      carfId.headOption.map(_.toUpper) match {
        case Some('Z') => None
        case Some('X') => None
        case _         => Some(stubFileName)
      }
    }

  def getRulesErrors(carfId: String): Option[Seq[BusinessRuleError]] = carfId.headOption.map(_.toUpper) match {
    case Some('Z') => None
    case Some('Y') => Some(Seq.empty)
    case Some('W') => Some(RulesErrorsStubData.manyErrors)
    case _         => Some(RulesErrorsStubData.fewErrors)
  }
}
