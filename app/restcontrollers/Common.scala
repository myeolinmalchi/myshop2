package restcontrollers

import dto.UserDto
import java.util.concurrent.TimeUnit
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Reads}
import play.api.mvc.Results.{BadRequest, Ok, Unauthorized}
import play.api.mvc.{AnyContent, Request, Result}
import restcontrollers.user.Common.{AuthResult, AuthSuccess, IncorrectAuth, IncorrectID, InvalidToken, NoToken, NotExistID}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success
import services.user.AccountService

object Common {
	
	val key = "testKey"
	val algo = JwtAlgorithm.HS256
	
	def withJson[A](f: A => Future[Result])
				   (implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
		request.body.asJson.map { body =>
			Json.fromJson[A](body) match {
				case JsSuccess(a, path) => f(a)
				case e @JsError(_) =>
					Future.successful(BadRequest)
			}
		}.getOrElse(Future.successful(BadRequest))
	}
	
	def withAnyJson(f: JsValue => Future[Result])
				   (implicit request: Request[AnyContent], ec: ExecutionContext): Future[Result] = {
		request.body.asJson match {
			case Some(value) => f(value)
			case None => Future.successful(BadRequest)
		}
	}
	
	val ROLE_USER = "user"
	val ROLE_SELLER = "seller"
	implicit class CustomResult(result: Result) {
		def withJwtHeader(id: String, role: String): Result = {
			val claims = JwtClaim (
				expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.DAYS.toSeconds(1)),
				issuedAt = Some(System.currentTimeMillis() / 1000),
				content =
					s""" {
					   | 	"id": "$id",
					   |	"role": "$role"
					   | }""".stripMargin
			)
			val token = JwtJson.encode(claims, key, algo)
			result.withHeaders("Authorization"-> token)
		}
	}
}
