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

import models.problem.MessageBlock.{Bullets, Para}

object RulesErrorsStubData {

  val fewErrors: Seq[BusinessRuleError] = Seq(
    BusinessRuleError(
      "50008",
      Seq.empty,
      Seq(
        Para(
          "MessageRefId element must be from 26 to 100 characters. It must also match the file name and " +
            "include the following in the order referenced:"
        ),
        Bullets(
          Seq(
            "‘GB’",
            "the same value as the year in the MessageSpec ReportingPeriod in the format ‘YYYY’",
            "‘GB’",
            "a hyphen (-)",
            "the 15-character RCASP ID from the MessageSpec SendingEntityIN",
            "a hyphen (-)",
            "1 to 75 characters of your choice to make the ID unique"
          )
        ),
        Para(
          "MessageRefId must also not include less than signs (<), greater than signs (>), colons (:), " +
            "straight double quotes (\"), apostrophes ('), ampersands (&), forward slashes (/), backslashes (\\), " +
            "vertical bars (|), question marks (?) or asterisks (*)."
        )
      )
    ),
    BusinessRuleError(
      "50010",
      Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001"),
      Seq(
        Para(
          "DocTypeIndic contains a value that indicates the file contains test data, like OECD10, OECD11, " +
            "OECD12 or OECD13. Replace the test data value with a value for real data, such as OECD0, OECD1, " +
            "OECD2 or OECD3."
        )
      )
    ),
    BusinessRuleError(
      "60011",
      Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001"),
      Seq(
        Para("CryptoUsers element must be provided if both the:"),
        Bullets(Seq("MessageTypeIndic is CARF701", "OtherNexus element is not present"))
      )
    ),
    BusinessRuleError(
      "80001",
      Seq(
        "GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-004",
        "GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-005"
      ),
      Seq(
        Para(
          "DocRefId element must be from 28 to 164 characters and include the following in the order referenced:"
        ),
        Bullets(
          Seq(
            "the same value as the MessageRefId for this submission",
            "a hyphen (-)",
            "1 to 63 characters of your choice to make the ID unique"
          )
        ),
        Para("For an OECD0 file, the DocRefId must match the previous submission.")
      )
    ),
    BusinessRuleError(
      "80002",
      Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-006"),
      Seq(
        Para("The CorrDocRefId provided does not match any DocRefId in our records for the same type of section.")
      )
    ),
    BusinessRuleError(
      "80010",
      Seq.empty,
      Seq(
        Para("Where the MessageTypeIndic is CARF701:"),
        Bullets(
          Seq(
            "the RCASP DocTypeIndic must be OECD0 or OECD1",
            "all CryptoUsers DocTypeIndic values must be OECD1"
          )
        ),
        Para("Where the MessageTypeIndic is CARF702:"),
        Bullets(
          Seq(
            "the RCASP DocTypeIndic must be OECD0, OECD2 or OECD3",
            "the CryptoUsers DocTypeIndic values must be OECD2, OECD3 or a combination of these values"
          )
        )
      )
    ),
    BusinessRuleError(
      "Temp 3",
      Seq.empty,
      Seq(
        Para(
          "SendingEntityIN is required and must contain the RCASP ID for the reporting cryptoasset service " +
            "provider in this report."
        )
      )
    ),
    BusinessRuleError(
      "Temp 9",
      Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-039"),
      Seq(
        Para("A TIN must have a value."),
        Para("If you have provided an actual TIN, the TIN element:"),
        Bullets(
          Seq(
            "must have an issuedBy attribute",
            "can only have an unknown attribute if the value of the attribute is ‘false’"
          )
        ),
        Para("For no TIN, the TIN value must be ‘NOTIN’ and the TIN element must:"),
        Bullets(
          Seq(
            "not have an issuedBy attribute",
            "have an unknown attribute with the value ‘true’"
          )
        )
      )
    ),
    BusinessRuleError(
      "Temp 10",
      Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-040"),
      Seq(Para("An IN must have issuedBy and INType attributes, unless the value is ‘NOTIN’."))
    ),
    BusinessRuleError(
      "Temp 18",
      Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-048"),
      Seq(
        Para(
          "When correcting or deleting the CryptoUsers, the CorrDocRefId must contain a DocRefId reported " +
            "under the same RCASP and ReportingPeriod."
        )
      )
    ),
    BusinessRuleError(
      "Temp 21",
      Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001"),
      Seq(
        Para(
          "The value for OtherNexus Nexus must be either the same or a weaker nexus than the value of RCASP Nexus."
        )
      )
    )
  )

  val manyErrors: Seq[BusinessRuleError] = (1 to 105).map { i =>
    BusinessRuleError(
      s"Temp $i",
      Seq.empty,
      Seq(MessageBlock.Para(s"Sample business rule error for testing purposes, item $i"))
    )
  }
}
