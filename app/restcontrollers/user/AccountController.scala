package restcontrollers.user

import dto.UserDto
import java.time.Clock
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc._
import restcontrollers.Common._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import services.user._

@Singleton
class AccountController @Inject()(ws: WSClient, cc: ControllerComponents)
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
				 Ok.withJwtHeader(user.userId.get, ROLE_USER)
			 } recover {
				 case ex: Exception => Unauthorized(Json.toJson(Map("error"-> ex.getMessage)))
			 }
		 }
	}
	
	def kakaoLogin: Action[AnyContent] = Action.async { implicit request =>
		withAnyJson { value =>
			val accessToken = (value \ "access_token").as[String]
			accountService getUserInfoByKakaoAccessToken accessToken flatMap { value =>
					accountService getUserOptionByEmail value._1 map {
						case Some(user) =>
							Ok(Json.toJson(Map("userId" -> user.userId.get)))
									.withJwtHeader(user.userId.get, ROLE_USER)
						case None =>
							Unauthorized(Json.toJson(Map(
								"email" -> value._1,
								"id" -> value._2
							)))
					}
			} recover {
				case ex: Exception => BadRequest
			}
		}
	}
	
	def kakaoRegister: Action[AnyContent] = Action.async { implicit request =>
		withJson[UserDto] { implicit user =>
			accountService.kakaoRegister map(_ => Ok) recover {
				case ex: Exception => BadRequest(Json.toJson(Map("error"-> ex.getMessage)))
			}
		}
	}
}