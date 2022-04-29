package services.user

import cats.data.OptionT
import com.google.inject.ImplementedBy
import common.validation.ValidationResultLib
import dto.UserDto
import scala.concurrent.Future

@ImplementedBy(classOf[AccountServiceImpl])
trait AccountService extends ValidationResultLib[Future]{
	
	def login(implicit user: UserDto): OptionT[Future, Boolean]
	def register(implicit user: UserDto): Future[Either[ValidationFailure, Unit]]
	def kakaoRegister(implicit user: UserDto): Future[Either[ValidationFailure, Unit]]
	def findId(email: String): Future[Option[String]]
	def getUser(userId: String): Future[UserDto]
	def getUserOption(userId: String): OptionT[Future, UserDto]
	def getUserOptionByEmail(email: String): Future[Option[UserDto]]
	def getUserInfoByKakaoAccessToken(accessToken: String): Future[(String, String)]
	def userIdExist(userId: String): Future[Boolean]
	
}