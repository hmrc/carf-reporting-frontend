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

import models.ReportType.*
import play.api.libs.json.*

case class ExtractedFileDetails(
    messageRefId: String,
    sendingEntityIn: String,
    rcaspName: Option[String],
    messageTypeIndic: MessageTypeIndic,
    hasOtherNexus: Boolean,
    hasCryptoUsers: Boolean,
    docTypeIndic: Option[DocTypeIndic],
    isTestData: Boolean,
    allCryptoUsersAreCorrections: Boolean,
    allCryptoUsersAreDeletions: Boolean
) {

  def getReportType: ReportType =
    if (isTestData) TestData
    else {
      messageTypeIndic match {
        case MessageTypeIndic.CARF701 =>
          if (hasOtherNexus && !hasCryptoUsers) NotificationOfReportingOutsideUk
          else if (docTypeIndic.contains(DocTypeIndic.OECD0)) AdditionalInformationForExistingReport
          else NewInformation
        case MessageTypeIndic.CARF702 =>
          docTypeIndic match {
            case Some(DocTypeIndic.OECD3)                            => DeletionOfExistingReport
            case Some(DocTypeIndic.OECD0) | Some(DocTypeIndic.OECD2) =>
              if (allCryptoUsersAreCorrections || !hasCryptoUsers) CorrectedInformationForExistingReport
              else if (docTypeIndic.contains(DocTypeIndic.OECD0) && allCryptoUsersAreDeletions)
                DeletedInformationForExistingReport
              else CorrectedAndDeletedInformationForExistingReport
            case _                                                   => ReportableInformationFallback
          }
        case MessageTypeIndic.CARF703 => NilReport
      }
    }

}

object ExtractedFileDetails {
  implicit val format: OFormat[ExtractedFileDetails] = Json.format[ExtractedFileDetails]
}
