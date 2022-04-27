package services.user

import com.google.inject.ImplementedBy
import dto.UserDto
import scala.concurrent.Future

@ImplementedBy(classOf[AccountServiceImpl])
trait AccountService {
	
	def login(implicit user: UserDto): Future[_]
	def register(implicit user: UserDto): Future[_]
	def findId(email: String): Future[Option[String]]
	def getUser(userId: String): Future[UserDto]
	def getUserOption(userId: String): Future[Option[UserDto]]
	
}