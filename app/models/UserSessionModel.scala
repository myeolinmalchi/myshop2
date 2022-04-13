package models

import java.time.LocalDateTime
import java.util.UUID
import play.api.Logging
import play.api.Logger
import play.api.mvc.Session
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
	
	def generateToken(userId: String, session: Session): String = {
		val token = s"$userId-user-${UUID.randomUUID().toString}"
		val time = LocalDateTime.now().plusHours(9)
		logger.info(s"[$time] User token has been generated: $token")
		sessions.put(session, UserSessionModel(token, userId, time))
		token
	}
}
