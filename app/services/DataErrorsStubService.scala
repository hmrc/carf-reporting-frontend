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

import models.problem.{DataErrorsStubData, SchemaError}

import javax.inject.Singleton

@Singleton
class DataErrorsStubService {

  private val stubFileName: String = "filename.xml"

  // TODO: replace with real lookups from UserAnswers once schema-validation logic exists (CARF-596).
  def getFileName(carfId: String): Option[String] =
    carfId.headOption match {
      case Some('Z') => None
      case Some('X') => None
      case _         => Some(stubFileName)
    }

  def getDataErrors(carfId: String): Option[Seq[SchemaError]] =
    carfId.headOption match {
      case Some('Z') => None
      case Some('Y') => Some(Seq.empty)
      case Some('X') => Some(DataErrorsStubData.fewErrors)
      case Some('W') => Some(DataErrorsStubData.manyErrors)
      case _         => Some(DataErrorsStubData.fewErrors)
    }
}
