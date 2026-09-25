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

package controllers.actions

import controllers.routes
import models.requests.OptionalDataRequest
import pages.DuplicateSubmissionLockPage
import play.api.mvc.{ActionFilter, Result, Results}
import utils.LoggerUtil.logInfo

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DuplicateSubmissionLockAction @Inject() (implicit val ec: ExecutionContext)
    extends ActionFilter[OptionalDataRequest] {

  override protected def executionContext: ExecutionContext = ec

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {

    val submitted = request.userAnswers.exists(_.get(DuplicateSubmissionLockPage).contains(true))

    if (submitted) {

      logInfo(
        s"[DuplicateSubmissionLockAction] Blocking request after send your file confirmation. path=${request.uri}"
      )

      Future.successful(
        Some(
          Results.Redirect(
            routes.PlaceholderController.onPageLoad("Should nav to /problem/page-unavailable (CARF-537)")
          )
        )
      )
    } else {
      Future.successful(None)
    }
  }
}
