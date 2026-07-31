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
import models.rcasp.{IndividualRcaspDetails, OrganisationRcaspDetails, RcaspContactDetails}
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

  val testMessageRefId =
    "GB2026GB-CARF01234567890-Cryptoasset-Reporting-Framework-XML-Report_for_2026_My-Company-Limited_0001"
  val testRcaspId      = "ZMCAR0123456787"
  val testRcaspName    = "Timmy's Turtles"

  val organisationRegisteredBusinessRcaspDetails =
    OrganisationRcaspDetails(
      RCASPID = testRcaspId,
      IsRCASPUser = true,
      RCASPName = testRcaspName,
      PrimaryContactDetails = None,
      SecondaryContactDetails = None
    )

  val individualRcaspDetails =
    IndividualRcaspDetails(
      RCASPID = "ZMCAR0123456788",
      IsRCASPUser = false,
      FirstName = "Nemona",
      LastName = "Champion",
      PrimaryContactDetails = Some(
        RcaspContactDetails(ContactName = "Clavell", EmailAddress = "clavell@uva.edu.org")
      )
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
}
