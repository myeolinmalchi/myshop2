package models

import java.time.LocalDateTime
import java.util.UUID
import play.api.Logging
import play.api.mvc.Session
import scala.collection.mutable

case class SellerSessionModel(token: String, sellerId: String, expiration: LocalDateTime)

object SellerSessionModel extends Logging{
	
	private val sessions = mutable.WeakHashMap.empty[Session, SellerSessionModel]
	
	def getSession(token: String): Option[SellerSessionModel] =
		sessions.find(_._2.token == token).map(_._2)
		
	def remSession(session: Session): Unit = {
		val time = LocalDateTime.now().plusHours(9)
		logger.info(s"[$time] Seller session has been removed.")
		sessions-=session
	}
	
	def generateToken(sellerId: String, session: Session): String = {
		val token = s"$sellerId-seller-${UUID.randomUUID().toString}"
		val time = LocalDateTime.now().plusHours(9)
		logger.info(s"[$time] Seller token has been generated: $token")
		sessions.put(session, SellerSessionModel(token, sellerId,time))
		token
	}
}
