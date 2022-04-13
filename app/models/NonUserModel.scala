package models

import java.util.UUID
import javax.inject.Singleton
import play.api.Logging
import play.api.mvc.Session

@Singleton
object NonUserModel extends Logging{
	def generateToken: String = {
		val token = s"0-non_user-${UUID.randomUUID().toString}"
		logger.info(s"Non-User token has been generated: $token")
		token
	}
}
