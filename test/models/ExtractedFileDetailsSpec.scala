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

import base.SpecBase
import models.DocTypeIndic.*
import models.MessageTypeIndic.*
import models.ReportType.*
import org.scalacheck.Gen

class ExtractedFileDetailsSpec extends SpecBase {

  val booleanGen: Gen[Boolean]                   = oneOf(Seq(true, false))
  val messageTypeIndicGen: Gen[MessageTypeIndic] = oneOf(Seq(CARF701, CARF702, CARF703))
  val docTypeIndicGen: Gen[DocTypeIndic]         = oneOf(Seq(OECD0, OECD1, OECD2, OECD3, OECD10, OECD11, OECD12, OECD13))

  "ExtractedFileDetails" - {
    ".getReportType" - {
      "when isTestData is true" in {
        val extractedFileDetails = ExtractedFileDetails(
          messageRefId = testMessageRefId,
          sendingEntityIn = testRcaspId,
          rcaspName = Some(testRcaspName),
          messageTypeIndic = messageTypeIndicGen.sample.get,
          hasOtherNexus = booleanGen.sample.get,
          hasCryptoUsers = booleanGen.sample.get,
          docTypeIndic = docTypeIndicGen.sample.get,
          isTestData = true,
          allCryptoUsersAreCorrections = booleanGen.sample.get,
          allCryptoUsersAreDeletions = booleanGen.sample.get
        )

        extractedFileDetails.getReportType mustBe TestData
      }

      "when isTestData is false" - {
        "when messageTypeIndic is CARF701" - {
          "when hasOtherNexus is true and hasCryptoUsers is false" in {
            val extractedFileDetails = ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testRcaspId,
              rcaspName = Some(testRcaspName),
              messageTypeIndic = CARF701,
              hasOtherNexus = true,
              hasCryptoUsers = false,
              docTypeIndic = docTypeIndicGen.sample.get,
              isTestData = false,
              allCryptoUsersAreCorrections = booleanGen.sample.get,
              allCryptoUsersAreDeletions = booleanGen.sample.get
            )

            extractedFileDetails.getReportType mustBe NotificationOfReportingOutsideUk
          }

          "when (hasOtherNexus is false or hasCryptoUsers is true) and DocTypeIndic is OECD0" in {
            val (hasOtherNexus, hasCryptoUsers) = oneOf(Seq((false, false), (true, true), (false, true))).sample.get

            val extractedFileDetails = ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testRcaspId,
              rcaspName = Some(testRcaspName),
              messageTypeIndic = CARF701,
              hasOtherNexus = hasOtherNexus,
              hasCryptoUsers = hasCryptoUsers,
              docTypeIndic = OECD0,
              isTestData = false,
              allCryptoUsersAreCorrections = booleanGen.sample.get,
              allCryptoUsersAreDeletions = booleanGen.sample.get
            )

            extractedFileDetails.getReportType mustBe AdditionalInformationForExistingReport
          }

          "when (hasOtherNexus is false or hasCryptoUsers is true) and DocTypeIndic is not OECD0" in {
            val (hasOtherNexus, hasCryptoUsers) = oneOf(Seq((false, false), (true, true), (false, true))).sample.get

            val extractedFileDetails = ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testRcaspId,
              rcaspName = Some(testRcaspName),
              messageTypeIndic = CARF701,
              hasOtherNexus = hasOtherNexus,
              hasCryptoUsers = hasCryptoUsers,
              docTypeIndic = oneOf(Seq(OECD1, OECD2, OECD3, OECD10, OECD11, OECD12, OECD13)).sample.get,
              isTestData = false,
              allCryptoUsersAreCorrections = booleanGen.sample.get,
              allCryptoUsersAreDeletions = booleanGen.sample.get
            )

            extractedFileDetails.getReportType mustBe NewInformation
          }
        }

        "when messageTypeIndic is CARF702" - {
          "when DocTypeIndic is OECD3" in {
            val extractedFileDetails = ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testRcaspId,
              rcaspName = Some(testRcaspName),
              messageTypeIndic = CARF702,
              hasOtherNexus = booleanGen.sample.get,
              hasCryptoUsers = booleanGen.sample.get,
              docTypeIndic = OECD3,
              isTestData = false,
              allCryptoUsersAreCorrections = booleanGen.sample.get,
              allCryptoUsersAreDeletions = booleanGen.sample.get
            )

            extractedFileDetails.getReportType mustBe DeletionOfExistingReport
          }

          "when DocTypeIndic is OECD0 or OECD2" - {
            "when allCryptoUsersAreCorrections is true or hasCryptoUsers is false" in {
              val (allCryptoUsersAreCorrections, hasCryptoUsers) =
                oneOf(Seq((false, false), (true, true), (true, false))).sample.get

              val extractedFileDetails = ExtractedFileDetails(
                messageRefId = testMessageRefId,
                sendingEntityIn = testRcaspId,
                rcaspName = Some(testRcaspName),
                messageTypeIndic = CARF702,
                hasOtherNexus = booleanGen.sample.get,
                hasCryptoUsers = hasCryptoUsers,
                docTypeIndic = oneOf(Seq(OECD0, OECD2)).sample.get,
                isTestData = false,
                allCryptoUsersAreCorrections = allCryptoUsersAreCorrections,
                allCryptoUsersAreDeletions = booleanGen.sample.get
              )

              extractedFileDetails.getReportType mustBe CorrectedInformationForExistingReport
            }

            "when allCryptoUsersAreCorrections is false, hasCryptoUsers is true, DocTypeIndic is OECD0 and allCryptoUsersAreDeletions is true" in {
              val extractedFileDetails = ExtractedFileDetails(
                messageRefId = testMessageRefId,
                sendingEntityIn = testRcaspId,
                rcaspName = Some(testRcaspName),
                messageTypeIndic = CARF702,
                hasOtherNexus = booleanGen.sample.get,
                hasCryptoUsers = true,
                docTypeIndic = OECD0,
                isTestData = false,
                allCryptoUsersAreCorrections = false,
                allCryptoUsersAreDeletions = true
              )

              extractedFileDetails.getReportType mustBe DeletedInformationForExistingReport
            }

            "when allCryptoUsersAreCorrections is false, hasCryptoUsers is true and (DocTypeIndic is OECD2 or allCryptoUsersAreDeletions is false)" in {
              val (docTypeIndic, allCryptoUsersAreDeletions) =
                oneOf(Seq((OECD0, false), (OECD2, true), (OECD2, false))).sample.get

              val extractedFileDetails = ExtractedFileDetails(
                messageRefId = testMessageRefId,
                sendingEntityIn = testRcaspId,
                rcaspName = Some(testRcaspName),
                messageTypeIndic = CARF702,
                hasOtherNexus = booleanGen.sample.get,
                hasCryptoUsers = true,
                docTypeIndic = docTypeIndic,
                isTestData = false,
                allCryptoUsersAreCorrections = false,
                allCryptoUsersAreDeletions = allCryptoUsersAreDeletions
              )

              extractedFileDetails.getReportType mustBe CorrectedAndDeletedInformationForExistingReport
            }
          }

          "when DocTypeIndic is not OECD3, OECD0 or OECD2" in {
            val extractedFileDetails = ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testRcaspId,
              rcaspName = Some(testRcaspName),
              messageTypeIndic = CARF702,
              hasOtherNexus = booleanGen.sample.get,
              hasCryptoUsers = booleanGen.sample.get,
              docTypeIndic = oneOf(Seq(OECD1, OECD10, OECD11, OECD12, OECD13)).sample.get,
              isTestData = false,
              allCryptoUsersAreCorrections = booleanGen.sample.get,
              allCryptoUsersAreDeletions = booleanGen.sample.get
            )

            extractedFileDetails.getReportType mustBe ReportableInformationFallback
          }
        }

        "when messageTypeIndic is CARF703" in {
          val extractedFileDetails = ExtractedFileDetails(
            messageRefId = testMessageRefId,
            sendingEntityIn = testRcaspId,
            rcaspName = Some(testRcaspName),
            messageTypeIndic = CARF703,
            hasOtherNexus = booleanGen.sample.get,
            hasCryptoUsers = booleanGen.sample.get,
            docTypeIndic = docTypeIndicGen.sample.get,
            isTestData = false,
            allCryptoUsersAreCorrections = booleanGen.sample.get,
            allCryptoUsersAreDeletions = booleanGen.sample.get
          )

          extractedFileDetails.getReportType mustBe NilReport
        }
      }
    }
  }
}
