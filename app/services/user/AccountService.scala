package services.user

import cats.data.OptionT
import com.google.inject.ImplementedBy
import common.validation.ValidationResultLib
import common.validation.ValidationResultLib.ValidationFailureTemp
import dto.{UserDto, UserRequestDto}
import scala.concurrent.Future

@ImplementedBy(classOf[AccountServiceImpl])
trait AccountService extends ValidationResultLib[Future]{
	
	def login(implicit user: UserRequestDto): OptionT[Future, Boolean]
	def regist(implicit user: UserRequestDto): Future[Either[ValidationFailureTemp, Unit]]
	def kakaoRegist(implicit user: UserRequestDto): Future[Either[ValidationFailureTemp, Unit]]
	def getUser(userId: String): OptionT[Future, UserDto]
	def getUserByEmail(email: String): OptionT[Future, UserDto]
	def getUserInfoByKakaoAccessToken(accessToken: String): Future[(String, String)]
	def userIdExist(userId: String): Future[Boolean]
	def updateUserPw(userPw: String): Future[Int]
	def updateName(name: String): Future[Int]
	
}