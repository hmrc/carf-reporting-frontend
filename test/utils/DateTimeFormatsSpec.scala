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

import base.SpecBase
import java.time.LocalDateTime

class DateTimeFormatsSpec extends SpecBase {

  "DateTimeFormats" - {

    ".dateTimeToString" - {

      "must format a morning time correctly (am)" in {
        val dateTime = LocalDateTime.of(2026, 8, 17, 9, 30)
        val result   = DateTimeFormats.dateTimeToString(dateTime)

        result mustEqual "17 August 2026 at 9:30am"
      }

      "must format an afternoon time correctly (pm)" in {
        val dateTime = LocalDateTime.of(2026, 8, 17, 16, 48)
        val result   = DateTimeFormats.dateTimeToString(dateTime)

        result mustEqual "17 August 2026 at 4:48pm"
      }

      "must format midnight as 'midnight' instead of 12:00am" in {
        val dateTime = LocalDateTime.of(2026, 8, 17, 0, 0)
        val result   = DateTimeFormats.dateTimeToString(dateTime)

        result mustEqual "17 August 2026 at midnight"
      }

      "must format noon as 'midday' instead of 12:00pm" in {
        val dateTime = LocalDateTime.of(2026, 8, 17, 12, 0)
        val result   = DateTimeFormats.dateTimeToString(dateTime)

        result mustEqual "17 August 2026 at midday"
      }
    }
  }
}
