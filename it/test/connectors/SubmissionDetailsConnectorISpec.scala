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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import itutil.ApplicationWithWiremock
import models.errors.ApiError.{InternalServerError, JsonValidationError, NotFoundError}
import models.fileSubmission.FileStatus.Passed
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import play.api.http.Status.*

class SubmissionDetailsConnectorISpec
    extends ApplicationWithWiremock
    with Matchers
    with ScalaFutures
    with IntegrationPatience {

  lazy val connector: SubmissionDetailsConnector = app.injector.instanceOf[SubmissionDetailsConnector]

  ".getFileStatus" - {
    val baseUrlPattern = "/carf-reporting/file-status/.*"

    "must successfully retrieve a FileStatus" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("\"Passed\"")
          )
      )

      val result = connector.getFileStatus(testUploadId).value.futureValue

      result mustBe Right(Passed)
    }

    "must return JsonValidationError when response JSON is invalid" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{"not": "the expected shape"}""")
          )
      )

      val result = connector.getFileStatus(testUploadId).value.futureValue

      result mustBe Left(JsonValidationError)
    }

    "must return NotFoundError given a 404 response" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result = connector.getFileStatus(testUploadId).value.futureValue

      result mustBe Left(NotFoundError)
    }

    "must return InternalServerError given a 400 response" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
          )
      )

      val result = connector.getFileStatus(testUploadId).value.futureValue

      result mustBe Left(InternalServerError)
    }

    "must return InternalServerError given a 500 response" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result = connector.getFileStatus(testUploadId).value.futureValue

      result mustBe Left(InternalServerError)
    }
  }

  ".getSubmissionDetailsByUploadId" - {
    val baseUrlPattern = "/carf-reporting/submission-details/.*"

    val testSubmissionDetailsJson: String =
      """
        |{
        |  "_id" : "123456",
        |  "carfId" : "XE0000123456789",
        |  "fileStatus" : "Failed",
        |  "fileName" : "test.xml",
        |  "extractedFileDetails" : {
        |    "messageRefId" : "GB2026GB-CARF01234567890-Cryptoasset-Reporting-Framework-XML-Report_for_2026_My-Company-Limited_0001",
        |    "sendingEntityIn" : "ZMCAR0123456787",
        |    "rcaspName" : "Timmy's Turtles",
        |    "messageTypeIndic" : "CARF701",
        |    "hasOtherNexus" : false,
        |    "hasCryptoUsers" : true,
        |    "docTypeIndic" : "OECD10",
        |    "isTestData" : true,
        |    "allCryptoUsersAreCorrections" : false,
        |    "allCryptoUsersAreDeletions" : false
        |  },
        |  "rcaspDetails" : {
        |    "RCASPID" : "ZMCAR0123456786",
        |    "IsRCASPUser" : false,
        |    "RCASPName" : "Timmy's Turtles",
        |    "PrimaryContactDetails" : {
        |      "ContactName" : "Nemona Champion",
        |      "EmailAddress" : "john.doe@example.com"
        |    },
        |    "SecondaryContactDetails" : {
        |      "ContactName" : "Clavell",
        |      "EmailAddress" : "clavell@uva.edu.org"
        |    }
        |  },
        |  "subscriptionDetails" : {
        |    "carfReference" : "XE0000123456789",
        |    "gbUser" : true,
        |    "primaryContact" : {
        |      "organisation" : {
        |        "name" : "John Doe"
        |      },
        |      "email" : "GroupRep@FATCACRS.com"
        |    },
        |    "secondaryContact" : {
        |      "organisation" : {
        |        "name" : "Jane Doe"
        |      },
        |      "email" : "GroupRep2@FATCACRS.com"
        |    }
        |  },
        |  "submissionTime" : {
        |    "$date" : {
        |      "$numberLong" : "1589978095789"
        |    }
        |  },
        |  "lastStatusUpdateTime" : {
        |    "$date" : {
        |      "$numberLong" : "1589978097789"
        |    }
        |  },
        |  "businessRuleErrors" : {
        |    "fileError" : [ {
        |      "code" : "50008",
        |      "details" : "MessageRefId element must be from 26 to 100 characters."
        |    } ],
        |    "recordError" : [ {
        |      "code" : "Temp 21",
        |      "details" : "The value for OtherNexus Nexus must be either the same or a weaker nexus than the value of RCASP Nexus.",
        |      "docRefIDInError" : [ "GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-004", "GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-005" ]
        |    } ]
        |  }
        |}
        |""".stripMargin

    "must successfully retrieve a SubmissionDetails" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(testSubmissionDetailsJson)
          )
      )

      val result = connector.getSubmissionDetailsByUploadId(testUploadId).value.futureValue

      result mustBe Right(submissionDetailsFailed)
    }

    "must return JsonValidationError when response JSON is invalid" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{"not": "the expected shape"}""")
          )
      )

      val result = connector.getSubmissionDetailsByUploadId(testUploadId).value.futureValue

      result mustBe Left(JsonValidationError)
    }

    "must return NotFoundError given a 404 response" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result = connector.getSubmissionDetailsByUploadId(testUploadId).value.futureValue

      result mustBe Left(NotFoundError)
    }

    "must return InternalServerError given a 400 response" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
          )
      )

      val result = connector.getSubmissionDetailsByUploadId(testUploadId).value.futureValue

      result mustBe Left(InternalServerError)
    }

    "must return InternalServerError given a 500 response" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result = connector.getSubmissionDetailsByUploadId(testUploadId).value.futureValue

      result mustBe Left(InternalServerError)
    }
  }

  ".getSubmissionDetailsByCarfId" - {
    val baseUrlPattern = "/carf-reporting/user-submission-details/.*"

    val testSubmissionDetailsJson: String =
      """
        |[ {
        |  "_id" : "123456",
        |  "carfId" : "XE0000123456789",
        |  "fileStatus" : "Failed",
        |  "fileName" : "test.xml",
        |  "extractedFileDetails" : {
        |    "messageRefId" : "GB2026GB-CARF01234567890-Cryptoasset-Reporting-Framework-XML-Report_for_2026_My-Company-Limited_0001",
        |    "sendingEntityIn" : "ZMCAR0123456787",
        |    "rcaspName" : "Timmy's Turtles",
        |    "messageTypeIndic" : "CARF701",
        |    "hasOtherNexus" : false,
        |    "hasCryptoUsers" : true,
        |    "docTypeIndic" : "OECD10",
        |    "isTestData" : true,
        |    "allCryptoUsersAreCorrections" : false,
        |    "allCryptoUsersAreDeletions" : false
        |  },
        |  "rcaspDetails" : {
        |    "RCASPID" : "ZMCAR0123456786",
        |    "IsRCASPUser" : false,
        |    "RCASPName" : "Timmy's Turtles",
        |    "PrimaryContactDetails" : {
        |      "ContactName" : "Nemona Champion",
        |      "EmailAddress" : "john.doe@example.com"
        |    },
        |    "SecondaryContactDetails" : {
        |      "ContactName" : "Clavell",
        |      "EmailAddress" : "clavell@uva.edu.org"
        |    }
        |  },
        |  "subscriptionDetails" : {
        |    "carfReference" : "XE0000123456789",
        |    "gbUser" : true,
        |    "primaryContact" : {
        |      "organisation" : {
        |        "name" : "John Doe"
        |      },
        |      "email" : "GroupRep@FATCACRS.com"
        |    },
        |    "secondaryContact" : {
        |      "organisation" : {
        |        "name" : "Jane Doe"
        |      },
        |      "email" : "GroupRep2@FATCACRS.com"
        |    }
        |  },
        |  "submissionTime" : {
        |    "$date" : {
        |      "$numberLong" : "1589978095789"
        |    }
        |  },
        |  "lastStatusUpdateTime" : {
        |    "$date" : {
        |      "$numberLong" : "1589978097789"
        |    }
        |  },
        |  "businessRuleErrors" : {
        |    "fileError" : [ {
        |      "code" : "50008",
        |      "details" : "MessageRefId element must be from 26 to 100 characters."
        |    } ],
        |    "recordError" : [ {
        |      "code" : "Temp 21",
        |      "details" : "The value for OtherNexus Nexus must be either the same or a weaker nexus than the value of RCASP Nexus.",
        |      "docRefIDInError" : [ "GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-004", "GB2026GB-XRCAS1234567890-CARF_Report2026_001-CryptoUsers-005" ]
        |    } ]
        |  }
        |}, {
        |  "_id" : "123456",
        |  "carfId" : "XE0000123456789",
        |  "fileStatus" : "Pending",
        |  "fileName" : "test.xml",
        |  "extractedFileDetails" : {
        |    "messageRefId" : "GB2026GB-CARF01234567890-Cryptoasset-Reporting-Framework-XML-Report_for_2026_My-Company-Limited_0001",
        |    "sendingEntityIn" : "ZMCAR0123456787",
        |    "rcaspName" : "Timmy's Turtles",
        |    "messageTypeIndic" : "CARF701",
        |    "hasOtherNexus" : false,
        |    "hasCryptoUsers" : true,
        |    "docTypeIndic" : "OECD10",
        |    "isTestData" : true,
        |    "allCryptoUsersAreCorrections" : false,
        |    "allCryptoUsersAreDeletions" : false
        |  },
        |  "rcaspDetails" : {
        |    "RCASPID" : "ZMCAR0123456788",
        |    "IsRCASPUser" : false,
        |    "FirstName" : "Nemona",
        |    "LastName" : "Champion",
        |    "PrimaryContactDetails" : {
        |      "ContactName" : "Nemona Champion",
        |      "EmailAddress" : "john.doe@example.com"
        |    }
        |  },
        |  "subscriptionDetails" : {
        |    "carfReference" : "XE0000123456789",
        |    "gbUser" : true,
        |    "primaryContact" : {
        |      "organisation" : {
        |        "name" : "John Doe"
        |      },
        |      "email" : "GroupRep@FATCACRS.com"
        |    },
        |    "secondaryContact" : {
        |      "organisation" : {
        |        "name" : "Jane Doe"
        |      },
        |      "email" : "GroupRep2@FATCACRS.com"
        |    }
        |  },
        |  "submissionTime" : {
        |    "$date" : {
        |      "$numberLong" : "1589978091789"
        |    }
        |  },
        | "lastStatusUpdateTime" : {
        |    "$date" : {
        |      "$numberLong" : "1589978097789"
        |    }
        |  },
        |  "businessRuleErrors" : {
        |    "fileError" : [ ],
        |    "recordError" : [ ]
        |  }
        |} ]
        |""".stripMargin

    "must successfully retrieve a list of SubmissionDetails" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(testSubmissionDetailsJson)
          )
      )

      val result = connector.getSubmissionDetailsByCarfId(testCarfId).value.futureValue

      result mustBe Right(Seq(submissionDetailsFailed, submissionDetailsPending))
    }

    "must return JsonValidationError when response JSON is invalid" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{"not": "the expected shape"}""")
          )
      )

      val result = connector.getSubmissionDetailsByCarfId(testCarfId).value.futureValue

      result mustBe Left(JsonValidationError)
    }

    "must return InternalServerError given a 400 response" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
          )
      )

      val result = connector.getSubmissionDetailsByCarfId(testCarfId).value.futureValue

      result mustBe Left(InternalServerError)
    }

    "must return InternalServerError given a 500 response" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result = connector.getSubmissionDetailsByCarfId(testCarfId).value.futureValue

      result mustBe Left(InternalServerError)
    }
  }
}
