package services

import dto.{AddressDto, CartDto, ProductDto, UserDto}
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api._
import models.Tables._
import scala.collection.mutable.Map
import scala.util.{Failure, Success, Try}
import java.security.MessageDigest
import java.math.BigInteger
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import common.encryption._
import scala.language.postfixOps
import slick.lifted.AbstractTable

/**
 * user 테이블 및 user_id를 외래키로 갖는 테이블의 데이터를 다루는 클래스
 * @author minsu
 * @version 1.0.0
 * 작성일 2022-03-17
 **/
@Singleton
class UserService(db: Database)(implicit ec: ExecutionContext) {
	
	def login(userId: String, userPw: String): Future[Option[Boolean]] = {
		import SHA256._
		db.run(Users.filter(u => u.userId === userId).result).map(_.headOption match {
			case Some(u) =>
				if(u.userPw.equals(encrypt(userPw))) Some(true) // 로그인 성공
				else Some(false) // 비밀번호 불일치
			case None => None // 존재하지 않는 계정
		})
	}
	
	private def validate(user: UserDto): Future[UserDto] =  {
		
		def checkPattern(str: String, pattern: String, errMsg: String): Future[String] =
			if(!str.matches(pattern)){
				Future.failed(new IllegalArgumentException(errMsg))
			} else Future.successful(str)
		
		def validUserId(userId: String): Future[String] = {
			val pattern = "^[a-z]+[a-z0-9]{5,19}$"
			if(!userId.matches(pattern)) {
				Future.failed(new IllegalArgumentException("유효하지 않은 아이디입니다."))
			} else{
				val matches = db.run(Users.filter(u => u.userId === userId).result)
				matches.flatMap { userRows =>
					if (userRows.isEmpty) Future.successful(userId)
					else throw new IllegalArgumentException("이미 존재하는 계정입니다.")
				}
			}
		}
		
		def validUserPw(userPw: String): Future[String] =
			checkPattern(userPw,
				"^(?=.*\\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,16}$",
				"유효하지 않은 비밀번호입니다.")
		
		def validName(name: String): Future[String] =
			checkPattern(name,
				"^[ㄱ-힣]+$",
				"유효하지 않은 이름입니다.")
				
		def validEmail(email: String): Future[String] = {
			val pattern = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$"
			if(!email.matches(pattern)) {
				Future.failed(new IllegalArgumentException("유효하지 않은 이메일입니다."))
			} else{
				val matches = db.run(Users.filter(u => u.email === email).result)
				matches.flatMap { userRows =>
					if (userRows.isEmpty) Future.successful(email)
					else throw new IllegalArgumentException("이미 존재하는 이메일입니다.")
				}
			}
		}
		
		def validPhone(phone: String): Future[String] =
			checkPattern(phone,
				"^\\d{3}-\\d{3,4}-\\d{4}$",
				"유효하지 않은 전화번호입니다.")
		
		for {
			userId <- validUserId(user.userId)
			userPw <- validUserPw(user.userPw)
			name <- validName(user.name)
			email <- validEmail(user.email)
			phone <- validPhone(user.phonenumber)
		} yield user
	}
	
	def register(user: UserDto): Future[Option[String]] = {
		import SHA256._
		validate(user).transform {
			case Success(result) =>
				db.run(Users.map(u => (u.userId, u.userPw, u.name, u.email, u.phonenumber))
						+= (user.userId, encrypt(user.userPw), user.name, user.email, user.phonenumber))
				Try(None)
			case Failure(e) => Try(Some(e.getMessage))
		}
	}
	
	def findId(email: String): Future[Option[String]] =
		db.run(Users.filter(u => u.email === email).result).map(_.headOption match {
			case Some(user) => Some(user.userId) // 아이디 찾기 성공
			case None => None // 존재하지 않는 이메일
		})
	
	
	
	
	def getCarts(implicit userId: String): Future[Seq[CartDto]] = {
		def getCartsByUserId(implicit useId: String): Future[List[CartDto]] =
			db run Carts.filter(cart => cart.userId === userId).result map(_ map { row =>
				CartDto(row)
			} toList )
			
		
		db.run(Carts.filter(cart => cart.userId === userId).result).map(_.map(CartDto(_)))
	}
	
	def getAddress(implicit userId: String): Future[Seq[AddressDto]] =
		db.run(UserAddresses.filter(addr => addr.userId === userId).result).map(_.map(AddressDto(_)))
	
}
