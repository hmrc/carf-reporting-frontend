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

import play.twirl.api.Html

// TODO: Remove when mapping of XmlErrors to SchemaErrors is implemented (CARF-591), until then keep for reference for the formatting
object DataErrorsStubData {

  val fewErrors: Seq[SchemaError] = Seq(
    SchemaError(4, Html("SendingEntityIN value must be the RCASP ID of the reporting cryptoasset service provider")),
    SchemaError(6, Html("Value is missing between ReceivingCountry element tags")),
    SchemaError(
      10,
      Html(
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
      )
    ),
    SchemaError(
      12,
      Html(
        """<p class="govuk-body govuk-!-margin-bottom-1">ReportingPeriod value must:</p>
          |<ul class="govuk-list govuk-list--bullet">
          |  <li>be in the format YYYY-MM-DD</li>
          |  <li>be between 2026 and the end of the current year</li>
          |  <li>include 31 as the day and 12 as the month</li>
          |</ul>
          |<p class="govuk-body govuk-!-margin-bottom-0">For example, 2026-12-31.</p>
          |""".stripMargin
      )
    ),
    SchemaError(15, Html("RCASP must contain either Entity or Individual")),
    SchemaError(26, Html("Value is missing between optional Street element tags")),
    SchemaError(36, Html("OtherNexus ResCountryCode attribute must contain an ISO country code")),
    SchemaError(
      41,
      Html(
        """<p class="govuk-body govuk-!-margin-bottom-1">DocRefId element must be from 28 to 164 characters and include the following in the order referenced:</p>
          |<ul class="govuk-list govuk-list--bullet">
          |  <li>the same value as the MessageRefId for this submission</li>
          |  <li>a hyphen (-)</li>
          |  <li>1 to 63 characters of your choice to make the ID unique</li>
          |</ul>
          |<p class="govuk-body govuk-!-margin-bottom-0">For an OECD0 file, the DocRefId must match the previous submission.</p>
          |""".stripMargin
      )
    ),
    SchemaError(120, Html("Amount element must have 2 decimal places. The amount must be 0 or more than 0")),
    SchemaError(260, Html("RelevantTransactions section is missing"))
  )

  val manyErrors: Seq[SchemaError] = (1 to 105).map { i =>
    SchemaError(i, Html(s"Sample schema error for testing purposes, line $i"))
  }
}
