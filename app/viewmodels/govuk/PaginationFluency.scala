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

package viewmodels.govuk

import config.Constants.{ONE, THREE, TWO}
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.*
import play.api.i18n.Messages
import play.api.mvc.Call

object pagination extends PaginationFluency

trait PaginationFluency {

  object PaginationViewModel {
    def apply(currentPage: Int, totalPages: Int, call: Int => Call)(implicit messages: Messages): Pagination = {
      val showPreviousPageLink: Boolean = currentPage > ONE
      val showNextPageLink: Boolean     = currentPage < totalPages

      val firstItem: Option[PaginationItem] = Option.when(currentPage > ONE)(
        PaginationItem(
          href = call(1).url,
          number = Some("1")
        )
      )

      val previousEllipses: Option[PaginationItem] = Option.when(currentPage > THREE)(ellipsePaginationItem)

      val previousItem: Option[PaginationItem] = Option.when(currentPage > TWO)(
        PaginationItem(
          href = call(currentPage - ONE).url,
          number = Some((currentPage - ONE).toString)
        )
      )

      val currentItem: Option[PaginationItem] = Some(
        PaginationItem(
          href = call(currentPage).url,
          number = Some(currentPage.toString),
          current = Some(true)
        )
      )

      val nextItem: Option[PaginationItem] = Option.when((totalPages - currentPage) > ONE)(
        PaginationItem(
          href = call(currentPage + ONE).url,
          number = Some((currentPage + ONE).toString)
        )
      )

      val nextEllipses: Option[PaginationItem] = Option.when((totalPages - currentPage) > TWO)(ellipsePaginationItem)

      val lastItem: Option[PaginationItem] = Option.when(currentPage < totalPages)(
        PaginationItem(
          href = call(totalPages).url,
          number = Some(totalPages.toString)
        )
      )

      val pageItems = Seq(
        firstItem,
        previousEllipses,
        previousItem,
        currentItem,
        nextItem,
        nextEllipses,
        lastItem
      ).flatten

      Pagination(
        items = Option.when(totalPages > ONE)(pageItems),
        next = Option.when(showNextPageLink)(
          PaginationLink(call(currentPage + ONE).url, Some(messages("site.next")))
        ),
        previous = Option.when(showPreviousPageLink)(
          PaginationLink(call(currentPage - ONE).url, Some(messages("site.previous")))
        )
      )
    }
  }

  private val ellipsePaginationItem = PaginationItem(
    href = "",
    ellipsis = Some(true)
  )
}
