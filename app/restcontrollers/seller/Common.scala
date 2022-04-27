package restcontrollers.seller

import dto.{SellerDto, UserDto}
import pdi.jwt.{JwtClaim, JwtJson}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request, Result}
import play.api.mvc.Results.Unauthorized
import restcontrollers.Common.{ROLE_SELLER, algo, key}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success
import services.seller.AccountService

object Common {
	
	sealed abstract class AuthResult[+T]
	case class AuthSuccess[+T](value: T) extends AuthResult[T] // 인증 성공
	case object InvalidToken extends AuthResult[Nothing] // 유효하지 않은 토큰
	case object IncorrectAuth extends AuthResult[Nothing] // 사용자 권한이 아님
	case object IncorrectID extends AuthResult[Nothing] // 두 ID가 일치하지 않음
	case object NotExistID extends AuthResult[Nothing] // db 상에 존재하지 않는 ID
	case object NoToken extends AuthResult[Nothing] // 토큰이 없음(사용자가 아님)
	
	case class SellerAuth(id: String = "")
						 (implicit request: Request[AnyContent], ec: ExecutionContext,
						  accountService: AccountService) {
		
		private def isExpired(claims: JwtClaim): Boolean =
			claims.expiration.getOrElse(0L) < System.currentTimeMillis() / 1000
		
		private def isTokenValid(token: String): Boolean =
			JwtJson.isValid(token, key, Seq(algo))
			
		private def getContentJson(claims: JwtClaim): (String, String) = {
			val json = Json.parse(claims.content)
			val role = (json \ "role").as[String]
			val id = (json \ "id").as[String]
			(role, id)
		}
		
		def getSeller: Future[AuthResult[SellerDto]] =
			request.headers.get("Authorization") match {
				case Some(token) if isTokenValid(token) =>
					JwtJson.decode(token, key, Seq(algo)) match {
						case Success(claims) if isExpired(claims) => Future.successful(InvalidToken)
						case Success(claims) =>
							val (role, id) = getContentJson(claims)
							if(role.equals(ROLE_SELLER)) {
								if(this.id.equals(id))
									accountService getSellerOption id map {
										case Some(seller) => AuthSuccess(seller)
										case None => NotExistID
									}
								else Future.successful(IncorrectID)
							} else Future.successful(IncorrectAuth)
						case _ => Future.successful(InvalidToken)
					}
				case Some(_) => Future.successful(InvalidToken)
				case None => Future.successful(NoToken)
			}
		
		def unAuthErrorJson(msg: String): Future[Result] =
			Future(Unauthorized(Json.toJson(Map("error" -> msg))))
		
		def auth(authSuccess: SellerDto => Future[Result],
				 invalidToken: => Future[Result] = unAuthErrorJson("유효하지 않은 토큰입니다."),
				 incorrectAuth: => Future[Result] = unAuthErrorJson("사용자 권한이 아닙니다."),
				 incorrectID: => Future[Result] = unAuthErrorJson("사용자 정보가 일치하지 않습니다."),
				 notExistID: => Future[Result] = unAuthErrorJson("존재하지 않는 사용자입니다."),
				 noToken: => Future[Result] = unAuthErrorJson("인증 정보를 찾을 수 없습니다.")): Future[Result] =
			getSeller flatMap {
				case AuthSuccess(result: SellerDto) => authSuccess(result)
				case InvalidToken => invalidToken
				case IncorrectAuth => incorrectAuth
				case IncorrectID => incorrectID
				case NotExistID => notExistID
				case NoToken => noToken
			}
	}
	
	def withUserAuth(id: String)(f: SellerDto => Future[Result])
					(implicit request: Request[AnyContent], ec: ExecutionContext,
					 accountService: AccountService): Future[Result] =
		SellerAuth(id).auth(f)
}
