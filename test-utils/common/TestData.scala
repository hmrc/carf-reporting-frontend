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

package common

import generators.Generators
import models.*
import models.DocTypeIndic.*
import models.MessageTypeIndic.*
import models.responses.*
import models.upscan.*
import models.upscan.UploadStatus.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow}
import viewmodels.govuk.all.{ActionItemViewModel, FluentActionItem, SummaryListRowViewModel, ValueViewModel}

import java.time.{Clock, Instant, ZoneId}

trait TestData extends Generators {

  val userAnswersId: String  = "id"
  val testInternalId: String = "12345"
  val testCarfId: String     = "XE0000123456789"

  private val utcZoneId     = "UTC"
  implicit val clock: Clock = Clock.fixed(Instant.parse("2020-05-20T12:34:56.789012Z"), ZoneId.of(utcZoneId))

  def emptyUserAnswers: UserAnswers =
    UserAnswers(id = userAnswersId, lastUpdated = Instant.now(clock))

  val testUploadId  = UploadId("123456")
  val testReference = Reference("11370e18-6e24-453e-b45a-76d3e32ea33d")

  val testFileName    = "test.xml"
  val testDownloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676"
  val testFileSize    = 987L
  val testChecksum    = "396f1"

  val postTarget = "http://localhost:9570/upscan/upload-proxy"

  val upscanInitiateResponse = UpscanInitiateResponse(
    testReference,
    postTarget,
    formFields = Map("formKey" -> "formValue")
  )

  val uploadedSuccessfully: UploadStatus.UploadedSuccessfully =
    UploadedSuccessfully(
      name = testFileName,
      downloadUrl = testDownloadUrl,
      size = testFileSize,
      checksum = testChecksum
    )

  val uploadRejected: UploadStatus.UploadRejected =
    UploadRejected(
      ErrorDetails(
        failureReason = "REJECTED",
        message = "Error message"
      )
    )

  val testMessageRefId =
    "GB2026GB-CARF01234567890-Cryptoasset-Reporting-Framework-XML-Report_for_2026_My-Company-Limited_0001"
  val testRcaspId      = "ZMCAR0123456787"
  val testRcaspName    = "Timmy's Turtles"

  val testFirstName   = "Nemona"
  val testLastName    = "Champion"
  val testContactName = s"$testFirstName $testLastName"
  val testEmail       = "john.doe@example.com"

  val organisationRegisteredBusinessRcaspDetails =
    OrganisationRcaspDetailsRcaspUser(
      RCASPID = testRcaspId,
      IsRCASPUser = true,
      RCASPName = testRcaspName
    )

  val organisationStandardRcaspDetails =
    OrganisationRcaspDetailsStandard(
      RCASPID = "ZMCAR0123456786",
      IsRCASPUser = false,
      RCASPName = testRcaspName,
      PrimaryContactDetails = RcaspContactDetails(ContactName = testContactName, EmailAddress = testEmail),
      SecondaryContactDetails = Some(RcaspContactDetails(ContactName = "Clavell", EmailAddress = "clavell@uva.edu.org"))
    )

  val individualRcaspDetails =
    IndividualRcaspDetails(
      RCASPID = "ZMCAR0123456788",
      IsRCASPUser = false,
      FirstName = testFirstName,
      LastName = testLastName,
      PrimaryContactDetails = RcaspContactDetails(ContactName = testContactName, EmailAddress = testEmail)
    )

  val displaySubscriptionIndividual = DisplaySubscriptionIndividual(testFirstName, testLastName)

  val displaySubscriptionOrganisation = DisplaySubscriptionOrganisation(testContactName)

  val displaySubscriptionResponseIndividual = DisplaySubscriptionResponse(
    success = DisplaySubscriptionSuccess(
      processingDate = "2024-01-25T09:26:17Z",
      carfSubscriptionDetails = DisplaySubscriptionDetails(
        carfReference = testCarfId,
        primaryContact = DisplaySubscriptionContact(
          individual = Some(
            DisplaySubscriptionIndividual(
              firstName = "Joe",
              lastName = "Smith"
            )
          ),
          organisation = None,
          email = "GroupRep@FATCACRS.com"
        ),
        secondaryContact = None
      )
    )
  )

  val subscriptionDetailsIndividual = SubscriptionDetails(
    primaryUserDetails = SubscriptionContactDetails("Joe Smith", "GroupRep@FATCACRS.com"),
    secondaryUserDetails = None
  )

  val displaySubscriptionResponseOrganisation = DisplaySubscriptionResponse(
    success = DisplaySubscriptionSuccess(
      processingDate = "2024-01-25T09:26:17Z",
      carfSubscriptionDetails = DisplaySubscriptionDetails(
        carfReference = testCarfId,
        primaryContact = DisplaySubscriptionContact(
          individual = None,
          organisation = Some(DisplaySubscriptionOrganisation(name = "John Doe")),
          email = "GroupRep@FATCACRS.com"
        ),
        secondaryContact = Some(
          DisplaySubscriptionContact(
            individual = None,
            organisation = Some(DisplaySubscriptionOrganisation(name = "Jane Doe")),
            email = "GroupRep2@FATCACRS.com"
          )
        )
      )
    )
  )

  val subscriptionDetailsOrganisation = SubscriptionDetails(
    primaryUserDetails = SubscriptionContactDetails("John Doe", "GroupRep@FATCACRS.com"),
    secondaryUserDetails = Some(SubscriptionContactDetails("Jane Doe", "GroupRep2@FATCACRS.com"))
  )

  lazy val testSummaryListRow: SummaryListRow =
    SummaryListRowViewModel(
      key = Key(Text("TEST Key")),
      value = ValueViewModel(Text("TEST Value")),
      actions = Seq(
        ActionItemViewModel(
          Text("TEST Action"),
          controllers.upload.routes.UploadXmlController.onPageLoad().url
        ).withVisuallyHiddenText("TEST HIDDEN TEXT")
      )
    )

  lazy val testSummaryList: SummaryList = SummaryList(Seq(testSummaryListRow))

  val extractedFileDetailsTestData: ExtractedFileDetails =
    ExtractedFileDetails(
      messageRefId = testMessageRefId,
      sendingEntityIn = testRcaspId,
      rcaspName = Some(testRcaspName),
      messageTypeIndic = CARF701,
      hasOtherNexus = false,
      hasCryptoUsers = true,
      docTypeIndic = OECD10,
      isTestData = true,
      allCryptoUsersAreCorrections = false,
      allCryptoUsersAreDeletions = false
    )

  val extractedFileDetailsNilReport: ExtractedFileDetails =
    ExtractedFileDetails(
      messageRefId = testMessageRefId,
      sendingEntityIn = testRcaspId,
      rcaspName = None,
      messageTypeIndic = CARF703,
      hasOtherNexus = false,
      hasCryptoUsers = false,
      docTypeIndic = OECD11,
      isTestData = false,
      allCryptoUsersAreCorrections = false,
      allCryptoUsersAreDeletions = false
    )
}
