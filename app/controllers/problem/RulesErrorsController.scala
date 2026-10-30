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

package controllers.problem

import config.{Constants, FrontendAppConfig}
import connectors.SubmissionDetailsConnector
import controllers.actions.*
import models.errors.BusinessRuleValidationErrors
import models.fileSubmission.FileStatus.Failed
import models.problem.BusinessRuleError
import models.problem.MessageBlock.Para
import models.upscan.UploadId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ErrorContentMapper.getContentForError
import utils.LoggerUtil.logWarn
import views.html.problem.RulesErrorsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.Try

class RulesErrorsController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    submissionDetailsConnector: SubmissionDetailsConnector,
    appConfig: FrontendAppConfig,
    val controllerComponents: MessagesControllerComponents,
    view: RulesErrorsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(uploadId: String): Action[AnyContent] = identify.async { implicit request =>
    submissionDetailsConnector.getSubmissionDetailsByUploadId(UploadId(uploadId)).value.map {
      case Right(fileDetails) if fileDetails.fileStatus == Failed =>
        if (fileDetails.businessRuleErrors.fileError.nonEmpty || fileDetails.businessRuleErrors.recordError.nonEmpty) {
          val processedErrors = processBusinessRuleErrors(fileDetails.businessRuleErrors)
          val hasMoreThanMax  = processedErrors.length > Constants.maxErrorsShown
          Ok(
            view(
              fileDetails.fileName,
              processedErrors.take(Constants.maxErrorsShown),
              hasMoreThanMax,
              appConfig.managementUrl
            )
          )
        } else {
          logWarn(s"[RulesErrorsController][onPageLoad] No business rule errors found in submission details")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      case Right(fileDetails)                                     =>
        logWarn(s"[RulesErrorsController][onPageLoad] Unexpected file status: ${fileDetails.fileStatus}")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Left(error)                                            =>
        logWarn(s"[RulesErrorsController][onPageLoad] Error retrieving file status: $error")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  private def processBusinessRuleErrors(
      businessRuleValidationErrors: BusinessRuleValidationErrors
  ): Seq[BusinessRuleError] = {

    val businessRuleFileErrors = businessRuleValidationErrors.fileError.map { error =>
      getContentForError(error.code).fold(
        BusinessRuleError(error.code, Seq("N/A"), Seq(Para(error.details.getOrElse(""))))
      )(identity)
    }

    val businessRuleRecordErrors = businessRuleValidationErrors.recordError.map { error =>
      getContentForError(error.code).fold(
        BusinessRuleError(error.code, error.docRefIDInError, Seq(Para(error.details.getOrElse(""))))
      ) { businessRuleError =>
        if (error.docRefIDInError.nonEmpty) {
          businessRuleError.copy(docRefIds = error.docRefIDInError)
        } else {
          businessRuleError
        }
      }
    }

    Try((businessRuleFileErrors ++ businessRuleRecordErrors).sortBy(_.errorCode.toInt))
      .getOrElse(
        (businessRuleFileErrors ++ businessRuleRecordErrors).sortBy(_.errorCode)
      )
  }
}
