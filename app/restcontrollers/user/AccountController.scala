package restcontrollers.user

import dto.UserDto
import java.time.Clock
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import restcontrollers.Common._
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import services.user._

@Singleton
class AccountController @Inject()(cc: ControllerComponents)
								 (implicit ec: ExecutionContext,
								  accountService: AccountService)
		extends AbstractController(cc) {
	
	private implicit val clock: Clock = Clock.systemUTC()
	
	def register: Action[AnyContent] = Action.async { implicit request =>
		withJson[UserDto] { implicit user =>
			accountService.register map (_ => Ok) recover {
				case ex: Exception => BadRequest(Json.toJson(Map("error"-> ex.getMessage)))
			}
		}
	}
	
	def login: Action[AnyContent] = Action.async { implicit request =>
		 withJson[UserDto] { implicit user =>
			 accountService.login map { _ =>
				 Ok(Json.toJson("")).withJwtHeader(user.userId.get, ROLE_USER)
			 } recover {
				 case ex: Exception => Unauthorized(Json.toJson(Map("error"-> ex.getMessage)))
			 }
		 }
	}
}