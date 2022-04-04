package models

import java.time.LocalDateTime
import java.util.UUID
import play.api.mvc.Session
import scala.collection.mutable

case class SellerSessionModel(token: String, sellerId: String, expiration: LocalDateTime)

object SellerSessionModel {
	
	private val sessions = mutable.WeakHashMap.empty[Session, SellerSessionModel]
	
	def getSession(token: String): Option[SellerSessionModel] = {
		println(sessions.size)
		sessions.find(_._2.token == token).map(_._2)
	}
	
	def remSession(session: Session): Unit = sessions-=session
	
	def generateToken(sellerId: String, session: Session): String = {
		val token = s"$sellerId-seller-${UUID.randomUUID().toString}"
		sessions.put(session, SellerSessionModel(token, sellerId, LocalDateTime.now().plusHours(9)))
		token
	}
}
