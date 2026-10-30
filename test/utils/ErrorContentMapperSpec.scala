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

package utils

import models.problem.BusinessRuleError
import models.problem.MessageBlock.{ListNoBullet, Para}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ErrorContentMapperSpec extends AnyWordSpec with Matchers {

  "ErrorContentMapper" must {

    "getContentForError" must {

      "return the BusinessRuleError when given an exact numeric error code without the prefix" in {
        val errorCode = "3"
        val expected  = BusinessRuleError(
          errorCode = "3",
          docRefIds = Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001"),
          message = Seq(Para("The file must not contain emoji Unicode characters"))
        )

        val result = ErrorContentMapper.getContentForError(errorCode)

        result mustBe Some(expected)
      }

      "return the BusinessRuleError when given an error code with the 'CARF Error Code ' prefix" in {
        val errorCode = "CARF Error Code 10"
        val expected  = BusinessRuleError(
          errorCode = "10",
          docRefIds = Seq("N/A"),
          message = Seq(
            Para("MessageRefId element value must be different to any you have used in a valid past submission")
          )
        )

        val result = ErrorContentMapper.getContentForError(errorCode)

        result mustBe Some(expected)
      }

      "return the BusinessRuleError when given an error code with the 'carf error Code ' (lowercase) prefix" in {
        val errorCode = "carf error Code 10"
        val expected  = BusinessRuleError(
          errorCode = "10",
          docRefIds = Seq("N/A"),
          message = Seq(
            Para("MessageRefId element value must be different to any you have used in a valid past submission")
          )
        )

        val result = ErrorContentMapper.getContentForError(errorCode)

        result mustBe Some(expected)
      }

      "return the BusinessRuleError with correct Seq[MessageBlock] content and N/A docRefIds for a complex error code" in {
        val errorCode       = "CARF Error Code 1"
        val expectedContent = Seq(
          Para("MessageRefId or DocRefId cannot include these characters:"),
          ListNoBullet(
            Seq(
              "' (apostrophe)",
              "\" (quotation mark)",
              "& (ampersand)",
              "< (less than)",
              "> (greater than)"
            )
          ),
          Para("In the rest of the file you must replace:"),
          ListNoBullet(
            Seq(
              "' (apostrophe) with &apos;",
              "\" (quotation mark) with &quot;",
              "& (ampersand) with &amp;",
              "< (less than) with &lt;",
              "> (greater than) with &gt;"
            )
          ),
          Para("You cannot include the following combinations of characters anywhere in the file:"),
          ListNoBullet(
            Seq(
              "-- (a double dash)",
              "/* (slash asterisk)",
              "&# (ampersand hash)"
            )
          )
        )

        val expected = BusinessRuleError(
          errorCode = "1",
          docRefIds = Seq("N/A"),
          message = expectedContent
        )

        val result = ErrorContentMapper.getContentForError(errorCode)

        result mustBe Some(expected)
      }

      "return None when given an unknown error code" in {
        val errorCode = "999"

        val result = ErrorContentMapper.getContentForError(errorCode)

        result mustBe None
      }

      "return None when given an empty string" in {
        val errorCode = ""

        val result = ErrorContentMapper.getContentForError(errorCode)

        result mustBe None
      }
    }
  }
}
