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

/** A single block within an error message cell. Used so the view can work out spacing (govuk-!-margin-bottom-3 / -4)
  * based on what the *previous* block was, per CARF-614 dev-mode.
  */
sealed trait MessageBlock

object MessageBlock {
  final case class Para(text: String) extends MessageBlock
  final case class Bullets(items: Seq[String]) extends MessageBlock
}

final case class BusinessRuleError(
    errorCode: String,
    docRefIds: Seq[String],
    message: Seq[MessageBlock]
)
