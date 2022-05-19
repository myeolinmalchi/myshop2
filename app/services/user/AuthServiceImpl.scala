package services.user

import cats.data.OptionT
import common.validation.ValidationResultLib
import common.validation.ValidationResultLib.ValidationFailure
import dto.UserDto
import javax.inject.Inject
import models.{NonUserModel, UserModel}
import pdi.jwt.{JwtClaim, JwtJson}
import play.api.libs.json.Json
import play.api.mvc.Results.{Forbidden, NotFound, Unauthorized}
import play.api.mvc.{AnyContent, Cookie, Request, Result}
import restcontrollers.Common.{ROLE_USER, algo, key}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import services.user.AuthService._

class AuthServiceImpl @Inject()()(implicit ec: ExecutionContext, userModel: UserModel)
	extends AuthService {
	
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
				userModel userIdDoesNotExist userId map (!_),
				onFailure = NotExistID
			)
		}
		
		def validation: Future[Either[AuthFailure, AuthSuccess[UserDto]]] = {
			val result: ValidationResult[AuthFailure, Unit] = (for {
				token <- OptionT.fromOption[Try](request.headers.get("Authorization"))
				claims <- OptionT.liftF(JwtJson.decode(token, key, Seq(algo)))
				(role, id) = getJsonContent(claims)
			} yield for {
				_ <- checkTokenValid(token)
				_ <- checkTokenNotExpired(claims)
				_ <- checkRole(role)
				_ <- checkUserId(id)
				_ <- checkUserExist(id)
			} yield ()).getOrElse(ValidationResult.failed[AuthFailure, Unit](NoToken)) match {
				case Success(rs: ValidationResult[AuthFailure, Unit]) => rs
				case Failure(_) => ValidationResult.failed(InvalidToken)
			}
			
			result.onSuccess(
				(userModel getUserById id)
					.map(AuthSuccess(_))
					.getOrElse(throw new Exception())
			)
		}
		
		def auth(authSuccess: UserDto => Future[Result]): Future[Result] =
			validation flatMap {
				case Right(AuthSuccess(user)) => authSuccess(user)
				case Left(InvalidToken) => Future(Unauthorized)
				case Left(ExpiredToken) => Future(Unauthorized)
				case Left(IncorrectAuth) => Future(Forbidden)
				case Left(IncorrectID) => Future(Forbidden)
				case Left(NotExistID) => Future(NotFound)
				case Left(NoToken) => Future(Unauthorized)
			}
		
		def auth(authSuccess: UserDto => Future[Result],
						 authFailure: => Future[Result]): Future[Result] =
			validation flatMap {
				case Right(AuthSuccess(user)) => authSuccess(user)
				case Left(_) => authFailure
			}
		
		def authAnd(f: String => Future[Result]): Future[Result] =
			validation flatMap {
				case Right(AuthSuccess(user)) => f(user.userId)
				case Left(_) =>
					request.cookies.get("idToken") match {
						case Some(idToken) => f(idToken.value)
						case None =>
							val token = NonUserModel.generateToken
							f(token) map (_.withCookies(
								new Cookie("idToken", token, Some(604800), httpOnly = false)))
					}
			}
		
		def authWithValidation[T](authSuccess: Future[Either[ValidationFailure, T]],
															authFailure: String => Future[Either[ValidationFailure, T]],
															result: Either[ValidationFailure, T] => Result): Future[Result] =
			validation flatMap {
				case Right(_) => authSuccess.map(result)
				case Left(_) =>
					request.cookies.get("idToken") match {
						case Some(idToken) => authFailure(idToken.value).map(result)
						case None =>
							val token = NonUserModel.generateToken
							authFailure(token).map(result)
								.map(_.withCookies(
									new Cookie("idToken", token, Some(604800), httpOnly = false)))
					}
				
			}
		
		
	}
	
	override def withUser(id: String)(f: UserDto => Future[Result])
											 (implicit request: Request[AnyContent], ec: ExecutionContext,
												accountService: AccountService): Future[Result] =
		UserAuth(id).auth(f)
	
	override def withUserId(id: String)(f: String => Future[Result])
												 (implicit request: Request[AnyContent], ec: ExecutionContext): Future[Result] =
		UserAuth(id).authAnd(f)
	
	override def withUserAndValidation[T](id: String)
																			 (authSuccess: Future[Either[ValidationFailure, T]],
																				authFailure: String => Future[Either[ValidationFailure, T]],
																				result: Either[ValidationFailure, T] => Result)
																			 (implicit request: Request[AnyContent], ec: ExecutionContext): Future[Result] =
		UserAuth(id).authWithValidation(authSuccess, authFailure, result)
	
	
}
