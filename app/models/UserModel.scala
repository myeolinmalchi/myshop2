package models

import common.encryption.SHA256
import dto.{SellerDto, UserDto}
import javax.inject.Singleton
import models.Tables.{Sellers, Users}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api._

@Singleton
class UserModel(db: Database)(implicit ec: ExecutionContext) {
	
	implicit val users = Users
	val common = new CommonModelApi(db)
	import common._
	
	def getUserById(userId: String): Future[Option[UserDto]] =
		selectOne[Users, UserDto](UserDto.newEntity){ user => user.userId === userId }
	
	def getUserByEmail(email: String): Future[Option[UserDto]] =
		selectOne[Users, UserDto](UserDto.newEntity){ user => user.email ===email }
	
	def insertUser(user: UserDto): Future[Int] =
		db.run(Users.map(u => (u.userId, u.userPw, u.name, u.email, u.phonenumber))
				+= (user.userId, SHA256.encrypt(user.userPw), user.name, user.email, user.phonenumber))
	
}
