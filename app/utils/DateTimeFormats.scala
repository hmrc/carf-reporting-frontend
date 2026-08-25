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

import config.Constants.ukTimeZoneStringId

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

object DateTimeFormats {

  private val datetimeFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("d MMMM yyyy 'at' h:mma")
    .withZone(ZoneId.of(ukTimeZoneStringId))

  def dateTimeToString(dateTime: LocalDateTime): String =
    dateTime
      .format(datetimeFormatter)
      .replace("12:00AM", "midnight")
      .replace("12:00PM", "midday")
      .replace("AM", "am")
      .replace("PM", "pm")
}
