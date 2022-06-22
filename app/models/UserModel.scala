package models

import cats.data.OptionT
import dto.UserDto
import dto.UserDto.RowToDto
import javax.inject.{Inject, Singleton}
import models.Tables.Users
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

@Singleton
class UserModel @Inject() (val dbConfigProvider: DatabaseConfigProvider)
						  (implicit ec: ExecutionContext)
	extends HasDatabaseConfigProvider[JdbcProfile] {
	
	def getAllUser: Future[List[UserDto]] =
		db run Users.result map(_.map(_.toDto).toList)
	
	def getUserById(userId: String): OptionT[Future, UserDto] =
		OptionT(db run Users.filter(_.userId === userId).result.headOption map(_.map(_.toDto)))
	
	def getUserPassword(userId: String): OptionT[Future, String] =
		OptionT(db run Users.filter(_.userId === userId).map(_.userPw).result.headOption)

	def checkUserExist(userId: String): Future[Option[String]] =
		db run Users.filter(_.userId === userId).map(_.userId).result.headOption
	
	def checkEmailExist(email: String): Future[Option[String]] =
		db run Users.filter(_.email === email).map(_.email).result.headOption
	
	def getUserByEmail(email: String): OptionT[Future, UserDto] =
		OptionT(db run Users.filter(_.email === email).result.headOption map(_.map(_.toDto)))
		
	def insertUser(user: UserDto): Future[Int] = {
		db run (Users.map(u => (u.userId, u.userPw, u.name, u.email, u.phonenumber))
			+= (user.userId, user.userPw, user.name, user.email, user.phonenumber))
	}
	
	def updateUserPw(userPw: String): Future[Int] =
		db run Users.map(_.userPw).update(userPw)
		
	def updateName(name: String): Future[Int] =
		db run Users.map(_.name).update(name)
	
	def userIdDoesNotExist(userId: String): Future[Boolean] =
		(db run Users.filter(_.userId === userId).map(_.userId).result).map(_.isEmpty)
		
	def emailDoesNotExist(email: String): Future[Boolean] =
		(db run Users.filter(_.email === email).map(_.email).result).map(_.isEmpty)
	
}
