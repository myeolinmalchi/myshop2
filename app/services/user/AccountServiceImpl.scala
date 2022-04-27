package services.user

import common.encryption.SHA256
import dto.UserDto
import javax.inject.{Inject, Singleton}
import models.UserModel
import scala.concurrent.{ExecutionContext, Future}
import services.user.Common._

@Singleton
class AccountServiceImpl @Inject() ()(implicit ec: ExecutionContext,
									  userModel: UserModel) extends AccountService {
	override def login(implicit user: UserDto): Future[_] =
		(userModel getUserPassword user.userId.get) flatMap {
			case Some(pw) =>
				if (pw.equals(SHA256.encrypt(user.userPw.get))) Future.successful()
				else Future.failed(new Exception("비밀번호가 일치하지 않습니다"))
			case None => Future.failed(new Exception("존재하지 않는 계정입니다."))
		}
	
	override def register(implicit user: UserDto): Future[_] =
		for {
			user <- accountValidation(user)
			aff <- userModel insertUser user
		} yield {
			if(aff == 1) ()
			else new Exception("회원가입에 실패했습니다.")
		}
	
	override def findId(email: String): Future[Option[String]] =
		userModel.getUserByEmail(email).map(_.flatMap(_.userId))
	
	override def getUser(userId: String): Future[UserDto] =
		userModel getUserById userId map(_.getOrElse(throw new Exception()))
	
	override def getUserOption(userId: String): Future[Option[UserDto]] =
		userModel getUserById userId
	
}
