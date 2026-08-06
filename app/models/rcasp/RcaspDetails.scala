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

package models.rcasp

import play.api.libs.json.{Json, OFormat, Reads, Writes}

sealed trait RcaspDetails {
  val RCASPID: String
  val IsRCASPUser: Boolean
  val PrimaryContactDetails: Option[RcaspContactDetails]
}

extension (rcaspDetails: RcaspDetails) {
  def getName: String =
    rcaspDetails match {
      case individual: IndividualRcaspDetails     => s"${individual.FirstName} ${individual.LastName}"
      case organisation: OrganisationRcaspDetails => organisation.RCASPName
    }
}

case class IndividualRcaspDetails(
    RCASPID: String,
    IsRCASPUser: Boolean,
    FirstName: String,
    LastName: String,
    PrimaryContactDetails: Option[RcaspContactDetails]
) extends RcaspDetails

case class OrganisationRcaspDetails(
    RCASPID: String,
    IsRCASPUser: Boolean,
    RCASPName: String,
    PrimaryContactDetails: Option[RcaspContactDetails],
    SecondaryContactDetails: Option[RcaspContactDetails]
) extends RcaspDetails

object RcaspDetails {

  implicit val reads: Reads[RcaspDetails] = Reads { json =>
    (json \ "RCASPName").validateOpt[String].flatMap {
      case Some(_) => json.validate[OrganisationRcaspDetails]
      case None    => json.validate[IndividualRcaspDetails]
    }
  }

  implicit val writes: Writes[RcaspDetails] = {
    case i: IndividualRcaspDetails   => IndividualRcaspDetails.format.writes(i)
    case o: OrganisationRcaspDetails => OrganisationRcaspDetails.format.writes(o)
  }
}

object IndividualRcaspDetails {
  implicit val format: OFormat[IndividualRcaspDetails] = Json.format[IndividualRcaspDetails]
}

object OrganisationRcaspDetails {
  implicit val format: OFormat[OrganisationRcaspDetails] = Json.format[OrganisationRcaspDetails]
}

case class RcaspContactDetails(ContactName: String, EmailAddress: String)

object RcaspContactDetails {
  implicit val format: OFormat[RcaspContactDetails] = Json.format[RcaspContactDetails]
}
