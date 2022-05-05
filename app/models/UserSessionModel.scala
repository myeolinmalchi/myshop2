package models

import dto.UserRequestDto
import java.time.LocalDateTime
import java.util.UUID
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, Request, Result, Session}
import scala.collection.mutable

case class UserSessionModel(token: String, userId: String, expiration: LocalDateTime)

object UserSessionModel {
	
	val logger = Logger(this.getClass)
	
	private val sessions = mutable.WeakHashMap.empty[Session, UserSessionModel]
	
	def getSession(token: String): Option[UserSessionModel] =
		sessions.find(_._2.token == token).map(_._2)
	
	def remSession(session: Session): Unit = {
		val time = LocalDateTime.now().plusHours(9)
		logger.info(s"[$time] User session has been removed.")
		sessions-=session
	}
	
	private def generateToken(implicit request: Request[AnyContent], user: UserRequestDto): String = {
		val token = s"${user.userId.get}-user-${UUID.randomUUID().toString}"
		val time = LocalDateTime.now().plusHours(9)
		logger.info(s"[$time] User token has been generated: $token")
		sessions.put(request.session, UserSessionModel(token, user.userId.get, time))
		token
	}
	
	def newSessionResult(implicit request: Request[AnyContent], user: UserRequestDto): Result =
		Ok(Json.toJson(true)).withSession("sessionToken" -> generateToken)
}
