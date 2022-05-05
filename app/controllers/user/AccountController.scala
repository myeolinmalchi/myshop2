package controllers.user

import cats.data.OptionT
import common.json.CustomJsonApi._
import controllers.Common._
import controllers.user.CommonApi._
import dto.UserRequestDto
import javax.inject.{Inject, Singleton}
import models.UserSessionModel
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import services.user._

@Singleton
class AccountController @Inject()(cc: ControllerComponents)
								 (implicit ec: ExecutionContext,
								  accountService: AccountService)
		extends AbstractController(cc){
	
	def loginPage: Action[AnyContent]= Action.async { implicit request =>
		withoutUser { Future(Ok(views.html.login())) }
	}
	
	def login: Action[AnyContent]= Action.async { implicit request =>
		withoutUser {
			withJsonDto[UserRequestDto] { implicit user =>
				(for {
					isCorrect <- accountService.login
					userId <- OptionT.fromOption[Future](user.userId)
				} yield if(isCorrect) {
					UserSessionModel.newSessionResult
				} else {
					BadRequest(Json.toJson(Map("error" -> "비밀번호가 일치하지 않습니다.")))
				}).getOrElse(BadRequest(Json.toJson(Map("error" -> "존재하지 않는 계정입니다."))))
			}
		}
	}
	
	def registerPage: Action[AnyContent]= Action.async { implicit request =>
		withoutUser{
			Future(Ok(views.html.register()))
		}
	}
	
	def register: Action[AnyContent]= Action async { implicit request =>
		withoutUser {
			withJsonDto[UserRequestDto] { implicit user =>
				accountService.regist trueOrError
			}
		}
	}
	
	def logout: Action[AnyContent]= Action.async { implicit request =>
		withUser { _ =>
			UserSessionModel.remSession(request.session)
			Future(Redirect(defaultPage).withSession(request.session - "sessionToken"))
		} endWith
	}
}
