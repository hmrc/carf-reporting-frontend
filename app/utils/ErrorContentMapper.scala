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

package utils

import models.problem.MessageBlock.{Bullets, ListNoBullet, Para}
import models.problem.{BusinessRuleError, MessageBlock}

object ErrorContentMapper {

  private val disallowedCharacters: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para("MessageRefId or DocRefId cannot include these characters:"),
      ListNoBullet(
        Seq(
          "' (apostrophe)",
          "\" (quotation mark)",
          "& (ampersand)",
          "< (less than)",
          "> (greater than)"
        )
      ),
      Para("In the rest of the file you must replace:"),
      ListNoBullet(
        Seq(
          "' (apostrophe) with &apos;",
          "\" (quotation mark) with &quot;",
          "& (ampersand) with &amp;",
          "< (less than) with &lt;",
          "> (greater than) with &gt;"
        )
      ),
      Para("You cannot include the following combinations of characters anywhere in the file:"),
      ListNoBullet(
        Seq(
          "-- (a double dash)",
          "/* (slash asterisk)",
          "&# (ampersand hash)"
        )
      )
    ),
    Seq("N/A")
  )

  private val noEmojis: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("The file must not contain emoji Unicode characters")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001")
  )

  private val nonPrintable: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para("Mandatory values in your file cannot consist of only non-printable characters like:"),
      ListNoBullet(
        Seq(
          "· (space)",
          "° (non-breaking space)",
          "→ (tab)",
          "¬ (soft hyphen)",
          "¶ (pilcrow)",
          "↵ (line break)",
          "···Page Break···"
        )
      )
    ),
    Seq(
      "GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001",
      "GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001"
    )
  )

  private val noTestData: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para(
        "DocTypeIndic cannot contain a value that indicates the file contains test data, like OECD10, OECD11, OECD12 or OECD13."
      ),
      Para("It must have a value for real data, such as OECD0, OECD1, OECD2 or OECD3.")
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001")
  )

  private val carfBodyReq: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para("If the MessageTypeIndic element is CARF701 or CARF702 then the CARF_OECD section must contain a CARFBody."),
      Para("If the MessageTypeIndic element is CARF703 then the CARF_OECD section must not contain a CARFBody.")
    ),
    Seq("N/A")
  )

  private val multiCarfBody: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("Each file can only have one CARFBody")),
    Seq("N/A")
  )

  private val matchingSEINandRCASPID: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para(
        "SendingEntityIN is required and must contain the RCASP ID for the reporting cryptoasset service provider in the report"
      )
    ),
    Seq("N/A")
  )

  private val uniqueMessageIdentifierRules: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para(
        "MessageRefId element must be from 26 to 100 characters. It must also match the file name and include the following, in this order:"
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
        "MessageRefId and file name can only include letters a to z, numbers 0 to 9, underscore (_), hyphens and full stops."
      )
    ),
    Seq("N/A")
  )

  private val duplicateUniqueMessageIdentifier: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("MessageRefId element value must be different to any you have used in a valid past submission")),
    Seq("N/A")
  )

  private val uniqueMessageIdentifierAndName: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para("Filename (excluding the .xml file extension) must match the MessageRefId."),
      Para(
        "MessageRefId and file name can only include letters a to z, numbers 0 to 9, underscore (_), hyphens and full stops."
      )
    ),
    Seq("N/A")
  )

  private val reportingPeriodFormat: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("ReportingPeriod must be in the format YYYY-MM-DD, with 12 as the month and 31 as the day")),
    Seq("N/A")
  )

  private val reportingPeriodEarliest: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("ReportingPeriod element date must be 2026 or later")),
    Seq("N/A")
  )

  private val reportingPeriodLatest: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("ReportingPeriod element date must be this year or earlier")),
    Seq("N/A")
  )

  private val unmatchedReportingPeriod: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para(
        "If RCASP section DocTypeIndic element is OECD0 or OECD2 then MessageSpec ReportingPeriod element value must be the same as the previous valid submission for this RCASP and reporting period"
      )
    ),
    Seq("N/A")
  )

  private val messageAndDocTypeCombo: (Seq[MessageBlock], Seq[String]) = (
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
    ),
    Seq("N/A")
  )

  private val OECD0: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("The value OECD0 can only be used in the RCASP element")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val corrDocRefIdExclusions = (
    Seq(Para("DocSpec element can only contain a CorrDocRefId element if DocTypeIndic is OECD2 or OECD3")),
    Seq(
      "GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001",
      "GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-002",
      "GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-003"
    )
  )

  private val missingCorrDocRefId: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("DocSpec element must contain a CorrDocRefId element if DocTypeIndic is OECD2 or OECD3")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val deletingRCASP: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para(
        "You can only delete an RCASP if you also delete all of the CryptoUsers sections for that RCASP in that reporting period"
      )
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val uniqueDocRefId: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para("DocRefId element must be from 28 to 164 characters and include the following in the order referenced:"),
      Bullets(
        Seq(
          "the same value as the MessageRefId for this submission",
          "a hyphen (-)",
          "1 to 63 characters of your choice to make the ID unique"
        )
      ),
      Para("For an OECD0 file, the DocRefId must match the previous submission.")
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val duplicateDocRefId: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para(
        "Unless you are submitting a resend (where the RCASP element value is OECD0), DocRefId element value must be different to any you have used elsewhere in the same file or in a valid past submission"
      )
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val unknownDocRefId: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para(
        "For a resend (where the RCASP element value is OECD0), DocRefId element value must be the same as for the last valid submission for this RCASP and reporting period"
      )
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val obsoleteDocRefId: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para(
        "For a resend (where the RCASP element value is OECD0), DocRefId element value must be the same as for the last valid correction submission for this RCASP and reporting period"
      )
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val docSpecAndCorrDocElements: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("DocSpec element must not contain a CorrMessageRefId element")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val unmatchedCorrDocRefId: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("CorrDocRefId must match a DocRefId for the same type of section in a previous submission")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val duplicateCorrDocRefId: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para("Each CorrDocRefId element value must:"),
      Bullets(
        Seq(
          "be different from all other CorrDocRefId values in the same file",
          "match the value of a DocRefId in a previous file that you are correcting or deleting"
        )
      )
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val unmatchedCorrDocRefIdValue: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para(
        "CorrDocRefId element value must match the value of the DocRefId element in the last valid file that contained that DocRefId."
      ),
      Para("This may be a correction file, not the first file you submitted.")
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val resendCannotIncludeCorrDocRefId: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("There cannot be a CorrDocRefId element if DocTypeIndic element value is OECD0")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001")
  )

  private val invalidCorrDocRefId: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para(
        "When correcting or deleting the CryptoUsers, the CorrDocRefId must contain a DocRefId reported under the same RCASP and ReportingPeriod"
      )
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2027_001-CryptoUsers-001")
  )

  private val unknownTIN: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("If unknown attribute value is true, then TIN value must be NOTIN")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val noTIN: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para("If TIN element value is NOTIN, then:"),
      Bullets(
        Seq(
          "TIN element unknown attribute value must be true",
          "there cannot be an issuedBy attribute for the TIN element"
        )
      )
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val tinRules: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para("A TIN must have a value."),
      Para("If you have provided an actual TIN, the TIN element:"),
      Bullets(
        Seq(
          "must have an issuedBy attribute",
          "can only have an unknown attribute if the value of the attribute is 'false'"
        )
      ),
      Para("For no TIN, the TIN value must be 'NOTIN' and the TIN element must:"),
      Bullets(
        Seq(
          "not have an issuedBy attribute",
          "have an Unknown attribute with the value 'true'"
        )
      )
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val innAttributes: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("There must be an issuedBy attribute unless the IIN element is NOTIN")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val inAttributes: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("An IN must have issuedBy and INType attributes, unless the value is 'NOTIN'")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val noIN: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("IN element value can only be NOTIN if INType attribute value is Other")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val birthDateRange: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para("BirthDate element value must be:"),
      Bullets(
        Seq(
          "after 1899, and",
          "no later than either today's date or 31-12 (31 December) of the reporting period, whichever is earliest"
        )
      )
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val indvBirthDateRange: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("If the RCASP is an Individual, then the BirthDate element must be 1900-01-01")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001")
  )

  private val missingNexus: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("RCASP section must have a Nexus element")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001")
  )

  private val rcaspNexus: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("The value for OtherNexus Nexus must be either the same or a weaker nexus than the value of RCASP Nexus")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001")
  )

  private val nexusValue: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("Nexus or OtherNexus element value cannot be CARF906 or CARF907")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001")
  )

  private val indvControllingPerson: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("If CryptoUsers is an Individual, it cannot have a Controlling Person section")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private val cryptoUsersElementRequirement: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para("CryptoUsers element must be provided if both the:"),
      Bullets(
        Seq(
          "MessageTypeIndic is CARF701",
          "OtherNexus element is not present"
        )
      )
    ),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-RCASP-001")
  )

  private val cryptoUsersElementRequirement2: (Seq[MessageBlock], Seq[String]) = (
    Seq(
      Para("There must be a CryptoUsers element if both:"),
      Bullets(
        Seq(
          "MessageTypeIndic element value is CARF702",
          "DocTypeIndic element value is OECD0"
        )
      )
    ),
    Seq("N/A")
  )

  private val cryptoassetPrice: (Seq[MessageBlock], Seq[String]) = (
    Seq(Para("Amount element cannot be a negative number")),
    Seq("GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-001")
  )

  private inline def mappedErrorCodeToContent = Map(
    "1"  -> disallowedCharacters,
    "3"  -> noEmojis,
    "4"  -> nonPrintable,
    "5"  -> noTestData,
    "6"  -> carfBodyReq,
    "7"  -> multiCarfBody,
    "8"  -> matchingSEINandRCASPID,
    "9"  -> uniqueMessageIdentifierRules,
    "10" -> duplicateUniqueMessageIdentifier,
    "11" -> uniqueMessageIdentifierAndName,
    "12" -> reportingPeriodFormat,
    "13" -> reportingPeriodEarliest,
    "14" -> reportingPeriodLatest,
    "15" -> unmatchedReportingPeriod,
    "16" -> messageAndDocTypeCombo,
    "17" -> OECD0,
    "18" -> corrDocRefIdExclusions,
    "19" -> missingCorrDocRefId,
    "20" -> deletingRCASP,
    "21" -> uniqueDocRefId,
    "22" -> duplicateDocRefId,
    "23" -> unknownDocRefId,
    "24" -> obsoleteDocRefId,
    "26" -> docSpecAndCorrDocElements,
    "27" -> unmatchedCorrDocRefId,
    "28" -> duplicateCorrDocRefId,
    "29" -> unmatchedCorrDocRefIdValue,
    "30" -> resendCannotIncludeCorrDocRefId,
    "31" -> invalidCorrDocRefId,
    "32" -> unknownTIN,
    "33" -> noTIN,
    "34" -> tinRules,
    "35" -> innAttributes,
    "36" -> inAttributes,
    "37" -> noIN,
    "38" -> birthDateRange,
    "39" -> indvBirthDateRange,
    "40" -> missingNexus,
    "41" -> rcaspNexus,
    "42" -> nexusValue,
    "43" -> indvControllingPerson,
    "44" -> cryptoUsersElementRequirement,
    "45" -> cryptoUsersElementRequirement2,
    "46" -> cryptoassetPrice
  )

  def getContentForError(errorCode: String): Option[BusinessRuleError] = {
    val reducedErrorCode = errorCode.toLowerCase.replace("carf error code ", "")

    mappedErrorCodeToContent.get(reducedErrorCode).map { case (content, docRefIds) =>
      BusinessRuleError(reducedErrorCode, docRefIds, content)
    }
  }
}
