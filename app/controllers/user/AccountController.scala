package controllers.user

import common.json.CustomJsonApi._
import controllers.Common._
import controllers.user.CommonApi._
import dto.UserDto
import javax.inject.{Inject, Singleton}
import models.UserSessionModel
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import services.user._

@Singleton
class AccountController @Inject()(cc: ControllerComponents)
								 (implicit ec: ExecutionContext,
								  userService: AccountService)
		extends AbstractController(cc){
	
	def loginPage: Action[AnyContent]= Action.async { implicit request =>
		withoutUser { Future(Ok(views.html.login())) }
	}
	
	def login: Action[AnyContent]= Action.async { implicit request =>
		withoutUser {
			withJsonDto[UserDto] { implicit user =>
				userService.login map ( _ => UserSessionModel.newSessionResult ) recover {
					case ex: Exception => ex toJsonError
				}
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
			withJsonDto[UserDto] { implicit user =>
				userService.register trueOrError
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
