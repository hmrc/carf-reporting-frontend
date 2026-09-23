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

<<<<<<<< HEAD:app/models/crypto/SensitiveJsObject.scala
package models.crypto

import play.api.libs.json.JsObject
import uk.gov.hmrc.crypto.Sensitive

case class SensitiveJsObject(override val decryptedValue: JsObject) extends Sensitive[JsObject]
========
package pages

import models.upscan.UploadStatus.UploadedSuccessfully
import play.api.libs.json.JsPath

case object UploadDetailsUserAnswers extends QuestionPage[UploadedSuccessfully] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "uploadDetails"

}
>>>>>>>> 17906d2 ([CARF-611] Implement Initial request to FTS/SDES):app/pages/UploadDetailsUserAnswers.scala
