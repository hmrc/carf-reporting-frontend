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

package forms

import forms.behaviours.FieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class UploadXmlFormProviderSpec extends FieldBehaviours {

  private val formProvider = new UploadXmlFormProvider()
  private val form         = formProvider()

  val validStringGen: Gen[String] = Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString)

  ".file-upload" - {

    val fieldName   = "file-upload"
    val requiredKey = "error.required"

    behave like fieldThatBindsValidData(form, fieldName, validStringGen)

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredKey))

    "must reduce duplicate spaces down to one space for second contact name" in {
      val result = form.bind(Map(fieldName -> "AAA  BBB T"))

      result.errors.isEmpty mustBe true
      result.value.get      mustBe "AAA BBB T"
    }
  }
}
