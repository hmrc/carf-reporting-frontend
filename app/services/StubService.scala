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

package services

import models.DocTypeIndic.*
import models.MessageTypeIndic.*
import models.filecheck.FileCheckStatus.{Failed, Passed, Virus}
import models.errors.ApiError.InternalServerError
import models.fileSubmission.FileStatus
import models.fileSubmission.FileStatus.*
import models.{ExtractedFileDetails, UserAnswers}
import pages.FileStatusPage
import repositories.SessionRepository
import models.fileSubmission.FileStatus
import models.fileSubmission.FileStatus.*
import types.ResultT

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StubService @Inject() (sessionRepository: SessionRepository) {

  private val testMessageRefId =
    "GB2026GB-CARF01234567890-Cryptoasset-Reporting-Framework-XML-Report_for_2026_My-Company-Limited_0001"

  private val testRcaspNameFromFile = "Timmy's Turtles"

  def getExtractedFileDetails(carfId: String, sendingEntityIn: String): Option[ExtractedFileDetails] =
    carfId.takeRight(1) match {
      case "1" => // TestData
        Some(
          ExtractedFileDetails(
            messageRefId = testMessageRefId,
            sendingEntityIn = sendingEntityIn,
            rcaspName = Some(testRcaspNameFromFile),
            messageTypeIndic = CARF701,
            hasOtherNexus = false,
            hasCryptoUsers = true,
            docTypeIndic = OECD10,
            isTestData = true,
            allCryptoUsersAreCorrections = false,
            allCryptoUsersAreDeletions = false
          )
        )
      case "2" => // NilReport
        Some(
          ExtractedFileDetails(
            messageRefId = testMessageRefId,
            sendingEntityIn = sendingEntityIn,
            rcaspName = None,
            messageTypeIndic = CARF703,
            hasOtherNexus = false,
            hasCryptoUsers = false,
            docTypeIndic = OECD11,
            isTestData = false,
            allCryptoUsersAreCorrections = false,
            allCryptoUsersAreDeletions = false
          )
        )
      case "3" => // NotificationOfReportingOutsideUk
        Some(
          ExtractedFileDetails(
            messageRefId = testMessageRefId,
            sendingEntityIn = sendingEntityIn,
            rcaspName = Some(testRcaspNameFromFile),
            messageTypeIndic = CARF701,
            hasOtherNexus = true,
            hasCryptoUsers = false,
            docTypeIndic = OECD11,
            isTestData = false,
            allCryptoUsersAreCorrections = false,
            allCryptoUsersAreDeletions = false
          )
        )
      case "4" => // NewInformation
        Some(
          ExtractedFileDetails(
            messageRefId = testMessageRefId,
            sendingEntityIn = sendingEntityIn,
            rcaspName = Some(testRcaspNameFromFile),
            messageTypeIndic = CARF701,
            hasOtherNexus = false,
            hasCryptoUsers = true,
            docTypeIndic = OECD11,
            isTestData = false,
            allCryptoUsersAreCorrections = false,
            allCryptoUsersAreDeletions = false
          )
        )
      case "5" => // AdditionalInformationForExistingReport
        Some(
          ExtractedFileDetails(
            messageRefId = testMessageRefId,
            sendingEntityIn = sendingEntityIn,
            rcaspName = Some(testRcaspNameFromFile),
            messageTypeIndic = CARF701,
            hasOtherNexus = false,
            hasCryptoUsers = true,
            docTypeIndic = OECD0,
            isTestData = false,
            allCryptoUsersAreCorrections = false,
            allCryptoUsersAreDeletions = false
          )
        )
      case "6" => // DeletionOfExistingReport
        Some(
          ExtractedFileDetails(
            messageRefId = testMessageRefId,
            sendingEntityIn = sendingEntityIn,
            rcaspName = Some(testRcaspNameFromFile),
            messageTypeIndic = CARF702,
            hasOtherNexus = false,
            hasCryptoUsers = true,
            docTypeIndic = OECD3,
            isTestData = false,
            allCryptoUsersAreCorrections = false,
            allCryptoUsersAreDeletions = false
          )
        )
      case "7" => // CorrectedInformationForExistingReport
        Some(
          ExtractedFileDetails(
            messageRefId = testMessageRefId,
            sendingEntityIn = sendingEntityIn,
            rcaspName = Some(testRcaspNameFromFile),
            messageTypeIndic = CARF702,
            hasOtherNexus = false,
            hasCryptoUsers = true,
            docTypeIndic = OECD2,
            isTestData = false,
            allCryptoUsersAreCorrections = true,
            allCryptoUsersAreDeletions = false
          )
        )
      case "8" => // DeletedInformationForExistingReport
        Some(
          ExtractedFileDetails(
            messageRefId = testMessageRefId,
            sendingEntityIn = sendingEntityIn,
            rcaspName = Some(testRcaspNameFromFile),
            messageTypeIndic = CARF702,
            hasOtherNexus = false,
            hasCryptoUsers = true,
            docTypeIndic = OECD0,
            isTestData = false,
            allCryptoUsersAreCorrections = false,
            allCryptoUsersAreDeletions = true
          )
        )
      case "9" => // CorrectedAndDeletedInformationForExistingReport
        Some(
          ExtractedFileDetails(
            messageRefId = testMessageRefId,
            sendingEntityIn = sendingEntityIn,
            rcaspName = Some(testRcaspNameFromFile),
            messageTypeIndic = CARF702,
            hasOtherNexus = false,
            hasCryptoUsers = true,
            docTypeIndic = OECD2,
            isTestData = false,
            allCryptoUsersAreCorrections = false,
            allCryptoUsersAreDeletions = false
          )
        )
      case "0" => // ReportableInformationFallback
        Some(
          ExtractedFileDetails(
            messageRefId = testMessageRefId,
            sendingEntityIn = sendingEntityIn,
            rcaspName = Some(testRcaspNameFromFile),
            messageTypeIndic = CARF702,
            hasOtherNexus = false,
            hasCryptoUsers = true,
            docTypeIndic = OECD12,
            isTestData = false,
            allCryptoUsersAreCorrections = false,
            allCryptoUsersAreDeletions = false
          )
        )

      case _ => None
    }

  def getFileStatus(carfId: String): ResultT[FileStatus] = {
    val status = carfId.dropRight(1).lastOption match {
      case Some('9') => UnexpectedError
      case Some('8') => UnprocessableErrorFile
      case Some('7') => VirusFound
      case Some('6') => Failed
      case Some('5') => Passed
      case _         => Pending
    }

    ResultT.fromValue(status)
  }
}

  def getFileStatus(carfId: String, userAnswers: UserAnswers)(implicit ec: ExecutionContext): ResultT[FileStatus] =
    if (carfId.takeRight(2).take(1) == "0") {
      ResultT.fromError(InternalServerError)
    } else {
      userAnswers
        .get(FileStatusPage)
        .fold {
          ResultT.fromFuture {
            for {
              updatedAnswers <- Future.fromTry(userAnswers.set(FileStatusPage, Pending))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Right(Pending)
          }
        } {
          case Pending                 =>
            val newStatus = carfId.takeRight(2).take(1) match {
              case "9" => UnexpectedError
              case "8" => UnprocessableErrorFile
              case "7" => VirusFound
              case "6" => Failed
              case "5" => Passed
              case _   => Pending
            }
            ResultT.fromFuture {
              for {
                updatedAnswers <- Future.fromTry(userAnswers.set(FileStatusPage, newStatus))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Right(newStatus)
            }
          case otherStatus: FileStatus => ResultT.fromValue(otherStatus)
        }
    }
}
