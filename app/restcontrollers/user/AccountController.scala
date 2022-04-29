package restcontrollers.user

import cats.data.OptionT
import common.validation.ValidationResultLib
import dto.UserDto
import java.time.Clock
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
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
		extends AbstractController(cc) with ValidationResultLib[Future] {
	
	private implicit val clock: Clock = Clock.systemUTC()
	
	def register: Action[AnyContent] = Action.async { implicit request =>
		withJson[UserDto] { implicit user =>
			accountService.register map {
				case Right(_) => Created
				case Left(failure) =>
					UnprocessableEntity(Json.toJson(Map("error" -> failure.msg)))
			} recover {
				case ex: NoSuchElementException =>
					BadRequest(Json.toJson(Map("error" -> ex.getMessage)))
			}
		}
	}
	
	implicit class CustomString(str: String) {
		def toJsonError: JsValue = toJsonMap("error")
		
		def toJsonMap(key: String): JsValue =
			Json.toJson(Map("key" -> str))
	}
	
	def login: Action[AnyContent] = Action.async { implicit request =>
		 withJson[UserDto] { implicit user =>
			 (for {
				 isCorrect <- accountService.login
				 userId <- OptionT.fromOption[Future](user.userId)
			 } yield if (isCorrect) {
				 Ok.withJwtHeader(userId, ROLE_USER)
			 } else {
				 BadRequest("비밀번호가 일치하지 않습니다.".toJsonError)
			 }).getOrElse(BadRequest("존재하지 않는 계정입니다.".toJsonError))
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