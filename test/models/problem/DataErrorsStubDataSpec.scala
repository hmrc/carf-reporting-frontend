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

package models.problem

import base.SpecBase

class DataErrorsStubDataSpec extends SpecBase {

  "DataErrorsStubData" - {

    "fewErrors must contain fewer than 100 entries" in {
      DataErrorsStubData.fewErrors.size must be < 100
    }

    "fewErrors must contain the expected line numbers from the prototype" in {
      DataErrorsStubData.fewErrors.map(_.lineNumber) mustEqual Seq(4, 6, 10, 12, 15, 26, 36, 41, 120, 260)
    }

    "manyErrors must contain more than 100 entries" in {
      DataErrorsStubData.manyErrors.size must be > 100
    }

    "manyErrors must contain sequential line numbers starting at 1" in {
      DataErrorsStubData.manyErrors.map(_.lineNumber) mustEqual (1 to 105)
    }
  }
}
