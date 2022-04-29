package models

import cats.data.OptionT
import common.encryption.SHA256
import dto.UserDto.RowToDto
import dto.{SellerDto, UserDto}
import javax.inject.{Inject, Singleton}
import models.Tables.{Sellers, Users}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api._
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

@Singleton
class UserModel @Inject() (val dbConfigProvider: DatabaseConfigProvider)
						  (implicit ec: ExecutionContext)
	extends HasDatabaseConfigProvider[JdbcProfile] {
	
	def getUserById(userId: String): OptionT[Future, UserDto] =
		OptionT(db run Users.filter(_.userId === userId).result.headOption map(_.map(_.toDto)))
	
	def getUserPassword(userId: String): OptionT[Future, String] =
		OptionT(db run Users.filter(_.userId === userId).map(_.userPw).result.headOption)

	def checkUserExist(userId: String): Future[Option[String]] =
		db run Users.filter(_.userId === userId).map(_.userId).result.headOption
	
	def checkEmailExist(email: String): Future[Option[String]] =
		db run Users.filter(_.email === email).map(_.email).result.headOption
	
	def getUserByEmail(email: String): Future[Option[UserDto]] =
		db run Users.filter(_.email === email).result.headOption map(_.map(_.toDto))
		
	def insertUser(user: UserDto): Future[Int] = {
		db run (Users.map(u => (u.userId, u.userPw, u.name, u.email, u.phonenumber))
			+= (user.userId.get, user.userPw.get, user.name.get, user.email.get, user.phonenumber.get))
	}
	
	def userIdDoesNotExist(userId: String): Future[Boolean] =
		(db run Users.filter(_.userId === userId).map(_.userId).result).map(_.isEmpty)
		
	def emailDoesNotExist(email: String): Future[Boolean] =
		(db run Users.filter(_.email === email).map(_.email).result).map(_.isEmpty)
	
}
