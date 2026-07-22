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

package models

enum ErrorCode(val code: String):
  case EntityTooLarge extends ErrorCode("entitytoolarge")
  case VirusFile extends ErrorCode("virusfile")
  case InvalidArgument extends ErrorCode("invalidargument")
  case OctetStream extends ErrorCode("octetstream")

object ErrorCode:
  def fromCode(code: String): Option[ErrorCode] = values.find(_.code.equalsIgnoreCase(code))

enum InvalidArgumentErrorMessage(val message: String):
  case InvalidFileNameLength extends InvalidArgumentErrorMessage("invalidfilenamelength")
  case TypeMismatch extends InvalidArgumentErrorMessage("typemismatch")
  case FileIsEmpty extends InvalidArgumentErrorMessage("fileisempty")
  case DisallowedCharacters extends InvalidArgumentErrorMessage("disallowedcharacters")

object InvalidArgumentErrorMessage:
  def fromMessage(message: String): Option[InvalidArgumentErrorMessage] =
    values.find(_.message.equalsIgnoreCase(message))
