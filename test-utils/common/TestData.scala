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
import models.errors.XmlError
import models.responses.*
import models.upscan.*
import models.upscan.UploadStatus.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow}
import viewmodels.govuk.all.{ActionItemViewModel, FluentActionItem, SummaryListRowViewModel, ValueViewModel}
import models.fileSubmission.FileStatus.Passed

import java.time.{Clock, Instant, LocalDateTime, ZoneId}

trait TestData extends Generators {

  val userAnswersId: String  = "id"
  val testInternalId: String = "12345"
  val testCarfId: String     = "XE0000123456789"

  private val utcZoneId = "UTC"
  val clock: Clock      = Clock.fixed(Instant.parse("2020-05-20T12:34:56.789012Z"), ZoneId.of(utcZoneId))

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

  val uploadSuccessDetails = UploadSuccessDetails(testFileName, testDownloadUrl)

  val xmlFewErrors: Seq[XmlError] = Seq(
    XmlError(4, "SendingEntityIN value must be the RCASP ID of the reporting cryptoasset service provider"),
    XmlError(6, "Value is missing between ReceivingCountry element tags"),
    XmlError(
      10,
      """<p class="govuk-body govuk-!-margin-bottom-1">MessageRefId element must be from 26 to 100 characters. It must also match the file name and include the following in the order referenced:</p>
          |<ul class="govuk-list govuk-list--bullet">
          |  <li>&lsquo;GB&rsquo;</li>
          |  <li>the same value as the year in the MessageSpec ReportingPeriod in the format &lsquo;YYYY&rsquo;</li>
          |  <li>&lsquo;GB&rsquo;</li>
          |  <li>a hyphen (-)</li>
          |  <li>the 15-character RCASP ID from the MessageSpec SendingEntityIN</li>
          |  <li>a hyphen (-)</li>
          |  <li>1 to 75 characters of your choice to make the ID unique</li>
          |</ul>
          |<p class="govuk-body govuk-!-margin-bottom-0">MessageRefId must also not include less than signs (<), greater than signs (>), colons (:), straight double quotes ("), apostrophes ('), ampersands (&amp;), forward slashes (/), backslashes (\), vertical bars (|), question marks (?) or asterisks (*).</p>
          |""".stripMargin
    ),
    XmlError(
      12,
      """<p class="govuk-body govuk-!-margin-bottom-1">ReportingPeriod value must:</p>
          |<ul class="govuk-list govuk-list--bullet">
          |  <li>be in the format YYYY-MM-DD</li>
          |  <li>be between 2026 and the end of the current year</li>
          |  <li>include 31 as the day and 12 as the month</li>
          |</ul>
          |<p class="govuk-body govuk-!-margin-bottom-0">For example, 2026-12-31.</p>
          |""".stripMargin
    ),
    XmlError(15, "RCASP must contain either Entity or Individual"),
    XmlError(26, "Value is missing between optional Street element tags"),
    XmlError(36, "OtherNexus ResCountryCode attribute must contain an ISO country code"),
    XmlError(
      41,
      """<p class="govuk-body govuk-!-margin-bottom-1">DocRefId element must be from 28 to 164 characters and include the following in the order referenced:</p>
          |<ul class="govuk-list govuk-list--bullet">
          |  <li>the same value as the MessageRefId for this submission</li>
          |  <li>a hyphen (-)</li>
          |  <li>1 to 63 characters of your choice to make the ID unique</li>
          |</ul>
          |<p class="govuk-body govuk-!-margin-bottom-0">For an OECD0 file, the DocRefId must match the previous submission.</p>
          |""".stripMargin
    ),
    XmlError(120, "Amount element must have 2 decimal places. The amount must be 0 or more than 0"),
    XmlError(260, "RelevantTransactions section is missing")
  )

  val xmlManyErrors: Seq[XmlError] = (1 to 101).map { i =>
    XmlError(i, s"Sample schema error for testing purposes, line $i")
  }

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
      docTypeIndic = Some(OECD10),
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
      docTypeIndic = None,
      isTestData = false,
      allCryptoUsersAreCorrections = false,
      allCryptoUsersAreDeletions = false
    )

  lazy val testDateTime: LocalDateTime = LocalDateTime.of(2026, 8, 17, 16, 48)

  val orgFileDetails = CachedFileDetails(
    Some(testDateTime),
    Passed,
    subscriptionDetailsOrganisation,
    organisationStandardRcaspDetails.copy(IsRCASPUser = true),
    Some(extractedFileDetailsTestData)
  )
}
