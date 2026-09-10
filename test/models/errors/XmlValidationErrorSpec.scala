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

package models.errors

import base.SpecBase
import play.api.libs.json.{JsError, Json}

class XmlValidationErrorSpec extends SpecBase {

  "XmlValidationError" - {
    "json reads" - {
      "must return InvalidXmlError when _type is InvalidXmlError" in {
        val json = """{"_type": "InvalidXmlError"}"""

        Json.parse(json).as[XmlValidationError] mustBe InvalidXmlError
      }

      "must return XmlErrors with schema errors when _type is XmlErrors" in {
        val json =
          """
            |{
            |  "errors": [
            |    {
            |      "lineNumber": 15,
            |      "errorCode": null,
            |      "errorMessage": "tag name \"MessageTypeIndic\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<Warning>"
            |    },
            |    {
            |      "lineNumber": 17,
            |      "errorCode": null,
            |      "errorMessage": "tag name \"ReportingPeriod\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<MessageTypeIndic>,<Warning>"
            |    },
            |    {
            |      "lineNumber": 18,
            |      "errorCode": null,
            |      "errorMessage": "tag name \"Timestamp\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<MessageTypeIndic>,<ReportingPeriod>,<Warning>"
            |    },
            |    {
            |      "lineNumber": 19,
            |      "errorCode": null,
            |      "errorMessage": "uncompleted content model. expecting: <Contact>,<MessageRefId>,<MessageTypeIndic>,<ReportingPeriod>,<Timestamp>,<Warning>"
            |    }
            |  ],
            |  "_type": "XmlErrors"
            |}
            |""".stripMargin

        val expectedResponse = XmlErrors(
          errors = Vector(
            XmlError(
              15,
              "tag name \"MessageTypeIndic\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<Warning>"
            ),
            XmlError(
              17,
              "tag name \"ReportingPeriod\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<MessageTypeIndic>,<Warning>"
            ),
            XmlError(
              18,
              "tag name \"Timestamp\" is not allowed. Possible tag names are: <Contact>,<MessageRefId>,<MessageTypeIndic>,<ReportingPeriod>,<Warning>"
            ),
            XmlError(
              19,
              "uncompleted content model. expecting: <Contact>,<MessageRefId>,<MessageTypeIndic>,<ReportingPeriod>,<Timestamp>,<Warning>"
            )
          )
        )

        Json.parse(json).as[XmlValidationError] mustBe expectedResponse
      }

      "must return JsonError" - {
        "when _type is unexpected value" in {
          val unexpectedValue = "RandomValue"
          val json            = s"""{"_type": "$unexpectedValue"}"""
          Json.parse(json).validate[XmlValidationError] mustBe JsError(
            s"""Unexpected value of _type: "$unexpectedValue""""
          )
        }

        "when _type is missing from JSON" in {
          val json = """{"type": "RandomValue"}"""
          Json.parse(json).validate[XmlValidationError] mustBe JsError("Missing _type field")
        }
      }
    }
  }
}
