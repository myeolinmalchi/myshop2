package models

import java.time.LocalDateTime
import java.util.UUID
import play.api.mvc.Session
import scala.collection.mutable

case class UserSessionModel(token: String, sellerId: String, expiration: LocalDateTime)

object UserSessionModel {
	
	private val sessions = mutable.WeakHashMap.empty[Session, UserSessionModel]
	
	def getSession(token: String): Option[UserSessionModel] =
		sessions.find(_._2.token == token).map(_._2)
	
	def remSession(session: Session): Unit = sessions-=session
	
	def generateToken(userId: String, session: Session): String = {
		val token = s"$userId-token-${UUID.randomUUID().toString}"
		sessions.put(session, UserSessionModel(token, userId, LocalDateTime.now().plusHours(9)))
		token
	}
}
