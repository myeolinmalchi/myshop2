package models

import java.util.UUID
import javax.inject.Singleton
import play.api.mvc.Session

@Singleton
object NonUserModel {
	def generateToken: String = s"0-non_user-${UUID.randomUUID().toString}"
}
