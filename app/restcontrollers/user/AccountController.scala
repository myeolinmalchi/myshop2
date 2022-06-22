package restcontrollers.user

import cats.data.OptionT
import common.validation.ValidationResultLib
import dto.UserRequestDto
import java.time.Clock
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import restcontrollers.Common._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import services.user._

@Singleton
class AccountController @Inject()(cc: ControllerComponents)
																 (implicit ec: ExecutionContext,
																	accountService: AccountService,
																	authService: AuthService)
	extends AbstractController(cc) with ValidationResultLib[Future] {
	
	private implicit val clock: Clock = Clock.systemUTC()
	
	import authService.withUser
	
	def regist: Action[AnyContent] = Action.async { implicit request =>
		withJson[UserRequestDto] { implicit user =>
			accountService.regist map {
				case Right(_) => Created
				case Left(failure) =>
					UnprocessableEntity(failure.msg.toJsonError)
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
		withJson[UserRequestDto] { implicit user =>
			(for {
				isCorrect <- accountService.login
				userId <- OptionT.fromOption[Future](user.userId)
			} yield if (isCorrect) {
				Ok.withJwtHeader(userId, ROLE_USER)
			} else {
				Unauthorized("비밀번호가 일치하지 않습니다.".toJsonError)
			}).getOrElse(NotFound("존재하지 않는 계정입니다.".toJsonError))
		}
	}
	
	def getUserData(userId: String): Action[AnyContent] = Action.async { implicit request =>
		withUser(userId) { user =>
			Future(Ok(Json.toJson(user)))
		}
	}
	
	def updateUser(userId: String): Action[AnyContent] = Action.async { implicit request =>
		withUser(userId) { _ =>
			withAnyJson { value =>
				val name = (value \ "name").asOpt[String]
				val userPw = (value \ "userPw").asOpt[String]
				(for {
					name <- OptionT.fromOption[Future](name)
					pw <- OptionT.fromOption[Future](userPw)
					_ <- OptionT.liftF(accountService updateName name)
					_ <- OptionT.liftF(accountService updateUserPw pw)
				} yield Ok).getOrElse(BadRequest) recover {
					case ex: Exception => BadRequest(ex.getMessage)
				}
			}
		}
	}
	
	def kakaoLogin: Action[AnyContent] = Action.async { implicit request =>
		withAnyJson { value =>
			val accessToken = (value \ "access_token").as[String]
			accountService getUserInfoByKakaoAccessToken accessToken flatMap { value =>
				val (email, id) = value
				accountService getUserByEmail email map { user =>
					Ok(Json.toJson(Map("userId" -> user.userId)))
						.withJwtHeader(user.userId, ROLE_USER)
				} getOrElse Unauthorized(Json.toJson(Map(
					"email" -> email, "id" -> id
				)))
			} recover {
				case ex: Exception => BadRequest(ex.getMessage.toJsonError)
			}
		}
	}
	
	def kakaoRegist: Action[AnyContent] = Action.async { implicit request =>
		withJson[UserRequestDto] { implicit user =>
			accountService.regist map {
				case Right(_) => Created
				case Left(failure) =>
					UnprocessableEntity(failure.msg.toJsonError)
			} recover {
				case ex: NoSuchElementException =>
					BadRequest(ex.getMessage.toJsonError)
			}
		}
	}
}