package services.user

import cats.data.OptionT
import common.validation.ValidationResultLib
import dto.UserDto
import javax.inject.Inject
import models.UserModel
import pdi.jwt.{JwtClaim, JwtJson}
import play.api.libs.json.Json
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{AnyContent, Request, Result}
import restcontrollers.Common.{ROLE_USER, algo, key}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class AuthServiceImpl @Inject() () (implicit ec: ExecutionContext, userModel: UserModel)
		extends AuthService {
	
	sealed abstract class AuthResult[T](value: T)
	case class AuthSuccess[T](value: T) extends AuthResult // 인증 성공
	case class AuthFailure(msg: String) extends AuthResult
	
	case object InvalidToken extends AuthFailure("") // 유효하지 않은 토큰
	case object ExpiredToken extends AuthFailure("") // 유효기간이 지난 토큰
	case object IncorrectAuth extends AuthFailure("") // 사용자 권한이 아님
	case object IncorrectID extends AuthFailure("") // 두 ID가 일치하지 않음
	case object NotExistID extends AuthFailure("") // db 상에 존재하지 않는 ID
	case object NoToken extends AuthFailure("") // 토큰이 없음(사용자가 아님)
	case class UserAuth(id: String = "")
					   (implicit request: Request[AnyContent], ec: ExecutionContext)
			extends ValidationResultLib[Future] {
		
		private def isExpired(claims: JwtClaim): Boolean =
			claims.expiration.getOrElse(0L) < System.currentTimeMillis() / 1000
		
		private def isTokenValid(token: String): Boolean =
			JwtJson.isValid(token, key, Seq(algo))
		
		private def getJsonContent(claims: JwtClaim): (String, String) = {
			val json = Json.parse(claims.content)
			val role = (json \ "role").as[String]
			val id = (json \ "id").as[String]
			(role, id)
		}
		
		def checkTokenValid(token: String): ValidationResult[AuthFailure, Unit] =
			ValidationResult.ensure(isTokenValid(token), onFailure = InvalidToken)
		
		def checkTokenNotExpired(claims: JwtClaim): ValidationResult[AuthFailure, Unit] =
			ValidationResult.ensure(!isExpired(claims), onFailure = ExpiredToken)
		
		def checkRole(role: String): ValidationResult[AuthFailure, Unit] =
			ValidationResult.ensure(role.equals(ROLE_USER), onFailure = IncorrectAuth)
		
		def checkUserId(userId: String): ValidationResult[AuthFailure, Unit] =
			ValidationResult.ensure(userId.equals(id), onFailure = IncorrectID)
		
		def checkUserExist(userId: String): ValidationResult[AuthFailure, Unit] = {
			ValidationResult.ensureM(
				userModel userIdDoesNotExist userId map(!_),
				onFailure = NotExistID
			)
		}
		
		def getUser: Future[Either[AuthFailure, AuthSuccess[UserDto]]] = {
			val result = (for {
				token <- OptionT.fromOption[Try](request.headers.get("Authorization"))
				claims <- OptionT.liftF(JwtJson.decode(token, key, Seq(algo)))
				(role, id) = getJsonContent(claims)
			} yield for {
				_ <- checkTokenValid(token)
				_ <- checkTokenNotExpired(claims)
				_ <- checkRole(role)
				_ <- checkUserId(id)
				_ <- checkUserExist(id)
			} yield ()).getOrElse(ValidationResult.failed(NoToken)) match {
				case Success(result: ValidationResult[AuthFailure, Unit]) => result
				case Failure(_) => ValidationResult.failed(InvalidToken)
			}
			
			result.onSuccess(
				(userModel getUserById id)
						.map(AuthSuccess(_))
						.getOrElse(throw new Exception())
			)
		}
		
		def unAuthErrorJson(msg: String): Future[Result] =
			Future(Unauthorized(Json.toJson(Map("error" -> msg))))
		
		def auth(authSuccess: UserDto => Future[Result],
				 invalidToken: => Future[Result] = unAuthErrorJson("유효하지 않은 토큰입니다."),
				 expiredToken: => Future[Result] = unAuthErrorJson("유효기간이 지난 토큰입니다."),
				 incorrectAuth: => Future[Result] = unAuthErrorJson("사용자 권한이 아닙니다."),
				 incorrectID: => Future[Result] = unAuthErrorJson("사용자 정보가 일치하지 않습니다."),
				 notExistID: => Future[Result] = unAuthErrorJson("존재하지 않는 사용자입니다."),
				 noToken: => Future[Result] = unAuthErrorJson("인증 정보를 찾을 수 없습니다.")): Future[Result] =
			getUser flatMap {
				case Right(result: AuthSuccess[UserDto]) => authSuccess(result.value)
				case Left(result) => result match {
					case InvalidToken => invalidToken
					case ExpiredToken => expiredToken
					case IncorrectAuth => incorrectAuth
					case IncorrectID => incorrectID
					case NotExistID => notExistID
					case NoToken => noToken
				}
			}
	}
	
	override def withUserAuth(id: String)(f: UserDto => Future[Result])
					(implicit request: Request[AnyContent], ec: ExecutionContext,
					 accountService: AccountService): Future[Result] =
		UserAuth(id).auth(f)
}
