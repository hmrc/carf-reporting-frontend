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

import cats.syntax.all.*
import models.DocTypeIndic.*
import models.MessageTypeIndic.*
import models.errors.ApiError.InternalServerError
import models.fileSubmission.FileStatus
import models.fileSubmission.FileStatus.*
import models.responses.*
import models.{CachedFileSubmissionDetails, ExtractedFileDetails, UserAnswers}
import pages.{ExtractedFileDetailsPage, FileStatusPage, RcaspDetailsPage, SubscriptionDetailsPage}
import repositories.SessionRepository
import types.ResultT

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class XmlFileDetailsStubService @Inject() (sessionRepository: SessionRepository, clock: Clock) {

  private inline val stubbedRCASPName = "testRcaspName"
  private inline val testCarfId       = "XE0000123456789"

  val organisationStandardRcaspDetails =
    OrganisationRcaspDetailsStandard(
      RCASPID = "ZMCAR0123456786",
      IsRCASPUser = false,
      RCASPName = stubbedRCASPName,
      PrimaryContactDetails = RcaspContactDetails(ContactName = "testContactName", EmailAddress = "stub@example.com"),
      SecondaryContactDetails = Some(RcaspContactDetails(ContactName = "Clavell", EmailAddress = "clavell@uva.edu.org"))
    )

  lazy val displaySubscriptionDetailsOrg: DisplaySubscriptionDetails = DisplaySubscriptionDetails(
    carfReference = testCarfId,
    gbUser = true,
    primaryContact = DisplaySubscriptionContact(
      individual = None,
      organisation = Some(DisplaySubscriptionOrganisation("John Doe")),
      email = "GroupRep@FATCACRS.com"
    ),
    secondaryContact = Some(
      DisplaySubscriptionContact(
        individual = None,
        organisation = Some(DisplaySubscriptionOrganisation("John Doe")),
        email = "GroupRep2@FATCACRS.com"
      )
    )
  )

  private def testDate = LocalDateTime.now(clock)

  def getCachedFileDetails(
      carfId: String,
      maybeUserAnswers: Option[UserAnswers],
      uploadId: String
  ): CachedFileSubmissionDetails = {
    val _ = uploadId // TODO Will be used to fetch correct file for RCASP Submission

    val maybeExtractedFileDetails = maybeUserAnswers.flatMap(_.get(ExtractedFileDetailsPage))
    val maybeRcaspDetails         = maybeUserAnswers.flatMap(_.get(RcaspDetailsPage))
    val maybeSubscriptionDetails  = maybeUserAnswers.flatMap(_.get(SubscriptionDetailsPage))

    (maybeExtractedFileDetails, maybeRcaspDetails, maybeSubscriptionDetails)
      .mapN { (extractedFileDetails, rcaspDetails, subscriptionDetails) =>
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          subscriptionDetails,
          rcaspDetails,
          Some(extractedFileDetails)
        )
      }
      .getOrElse(hardcodedCachedFileDetails(carfId))
  }

  private def hardcodedCachedFileDetails(carfId: String): CachedFileSubmissionDetails = {
    val testMessageRefId      =
      "GB2026GB-CARF01234567890-Cryptoasset-Reporting-Framework-XML-Report_for_2026_My-Company-Limited_0001"
    val testRcaspNameFromFile = "Timmy's Turtles"
    val testSendingEntityIn   = "ZMCAR0123456786"

    carfId.takeRight(1) match {
      case "1" => // TestData
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          displaySubscriptionDetailsOrg,
          organisationStandardRcaspDetails,
          Some(
            ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testSendingEntityIn,
              rcaspName = Some(testRcaspNameFromFile),
              messageTypeIndic = CARF701,
              hasOtherNexus = false,
              hasCryptoUsers = true,
              docTypeIndic = Some(OECD10),
              isTestData = true,
              allCryptoUsersAreCorrections = false,
              allCryptoUsersAreDeletions = false
            )
          )
        )
      case "2" => // NilReport
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          displaySubscriptionDetailsOrg,
          organisationStandardRcaspDetails,
          Some(
            ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testSendingEntityIn,
              rcaspName = None,
              messageTypeIndic = CARF703,
              hasOtherNexus = false,
              hasCryptoUsers = false,
              docTypeIndic = Some(OECD11),
              isTestData = false,
              allCryptoUsersAreCorrections = false,
              allCryptoUsersAreDeletions = false
            )
          )
        )
      case "3" => // NotificationOfReportingOutsideUk
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          displaySubscriptionDetailsOrg,
          organisationStandardRcaspDetails,
          Some(
            ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testSendingEntityIn,
              rcaspName = Some(testRcaspNameFromFile),
              messageTypeIndic = CARF701,
              hasOtherNexus = true,
              hasCryptoUsers = false,
              docTypeIndic = Some(OECD11),
              isTestData = false,
              allCryptoUsersAreCorrections = false,
              allCryptoUsersAreDeletions = false
            )
          )
        )
      case "4" => // NewInformation
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          displaySubscriptionDetailsOrg,
          organisationStandardRcaspDetails,
          Some(
            ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testSendingEntityIn,
              rcaspName = Some(testRcaspNameFromFile),
              messageTypeIndic = CARF701,
              hasOtherNexus = false,
              hasCryptoUsers = true,
              docTypeIndic = Some(OECD11),
              isTestData = false,
              allCryptoUsersAreCorrections = false,
              allCryptoUsersAreDeletions = false
            )
          )
        )
      case "5" => // AdditionalInformationForExistingReport
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          displaySubscriptionDetailsOrg,
          organisationStandardRcaspDetails,
          Some(
            ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testSendingEntityIn,
              rcaspName = Some(testRcaspNameFromFile),
              messageTypeIndic = CARF701,
              hasOtherNexus = false,
              hasCryptoUsers = true,
              docTypeIndic = Some(OECD0),
              isTestData = false,
              allCryptoUsersAreCorrections = false,
              allCryptoUsersAreDeletions = false
            )
          )
        )
      case "6" => // DeletionOfExistingReport
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          displaySubscriptionDetailsOrg.copy(secondaryContact = None),
          organisationStandardRcaspDetails.copy(IsRCASPUser = true),
          Some(
            ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testSendingEntityIn,
              rcaspName = Some(testRcaspNameFromFile),
              messageTypeIndic = CARF702,
              hasOtherNexus = false,
              hasCryptoUsers = true,
              docTypeIndic = Some(OECD3),
              isTestData = false,
              allCryptoUsersAreCorrections = false,
              allCryptoUsersAreDeletions = false
            )
          )
        )
      case "7" => // CorrectedInformationForExistingReport
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          displaySubscriptionDetailsOrg,
          organisationStandardRcaspDetails.copy(IsRCASPUser = true),
          Some(
            ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testSendingEntityIn,
              rcaspName = Some(testRcaspNameFromFile),
              messageTypeIndic = CARF702,
              hasOtherNexus = false,
              hasCryptoUsers = true,
              docTypeIndic = Some(OECD2),
              isTestData = false,
              allCryptoUsersAreCorrections = true,
              allCryptoUsersAreDeletions = false
            )
          )
        )
      case "8" => // DeletedInformationForExistingReport
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          displaySubscriptionDetailsOrg,
          organisationStandardRcaspDetails,
          Some(
            ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testSendingEntityIn,
              rcaspName = Some(testRcaspNameFromFile),
              messageTypeIndic = CARF702,
              hasOtherNexus = false,
              hasCryptoUsers = true,
              docTypeIndic = Some(OECD0),
              isTestData = false,
              allCryptoUsersAreCorrections = false,
              allCryptoUsersAreDeletions = true
            )
          )
        )
      case "9" => // CorrectedAndDeletedInformationForExistingReport
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          displaySubscriptionDetailsOrg,
          organisationStandardRcaspDetails,
          Some(
            ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testSendingEntityIn,
              rcaspName = Some(testRcaspNameFromFile),
              messageTypeIndic = CARF702,
              hasOtherNexus = false,
              hasCryptoUsers = true,
              docTypeIndic = Some(OECD2),
              isTestData = false,
              allCryptoUsersAreCorrections = false,
              allCryptoUsersAreDeletions = false
            )
          )
        )
      case "0" => // ReportableInformationFallback
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          displaySubscriptionDetailsOrg,
          organisationStandardRcaspDetails,
          Some(
            ExtractedFileDetails(
              messageRefId = testMessageRefId,
              sendingEntityIn = testSendingEntityIn,
              rcaspName = Some(testRcaspNameFromFile),
              messageTypeIndic = CARF702,
              hasOtherNexus = false,
              hasCryptoUsers = true,
              docTypeIndic = Some(OECD12),
              isTestData = false,
              allCryptoUsersAreCorrections = false,
              allCryptoUsersAreDeletions = false
            )
          )
        )
      case _   =>
        CachedFileSubmissionDetails(
          Some(testDate),
          Passed,
          displaySubscriptionDetailsOrg,
          organisationStandardRcaspDetails,
          None
        )
    }
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
