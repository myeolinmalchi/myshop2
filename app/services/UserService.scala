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
import models.{CartModel, UserModel}
import play.api.libs.oauth.{ConsumerKey, OAuth, ServiceInfo}
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
	
	private val userModel = new UserModel(db)
	private val cartModel = new CartModel(db)
	
	def login(userId: String, userPw: String): Future[Option[Boolean]] =
		userModel.getUserById(userId) map {
			case Some(u) =>
				if (u.userPw.equals(SHA256.encrypt(userPw))) Some(true) // 로그인 성공
				else Some(false) // 비밀번호 불일치
			case None => None // 존재하지 않는 계정
		}
	
	def accountValidation(user: UserDto): Future[UserDto] = {
		def patterns(implicit key: String) = Map (
			"userId" -> "^[a-z]+[a-z0-9]{5,19}$",
			"userPw" -> "^(?=.*\\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,16}$",
			"name" -> "^[ㄱ-힣]+$",
			"email" -> "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$",
			"phone" -> "^\\d{3}-\\d{3,4}-\\d{4}$"
		)(key)
		def noneMatchedMsg(implicit key: String): String = Map (
			"userId" -> "유효하지 않은 아이디입니다.",
			"userPw" -> "유효하지 않은 비밀번호입니다.",
			"name"  -> "유효하지 않은 이름입니다.",
			"email" -> "유효하지 않은 이메일입니다.",
			"phone" -> "유효하지 않은 전화번호입니다."
		)(key)
		def checkPattern(str: String)(implicit key: String): Future[String] =
			if (!str.matches(patterns))
				Future.failed(new IllegalArgumentException(noneMatchedMsg))
			else Future.successful(str)
		
		def validUserId(userId: String): Future[String] =
			checkPattern(userId)("userId") transform  {
				case Success(userId) => Try(userId)
				case Failure(e)	 => Failure(e)
			} flatMap { userModel getUserById(_) flatMap ({
				case None => Future.successful(userId)
				case Some(_) => throw new IllegalArgumentException("이미 존재하는 계정입니다.")
			})}
		def validUserPw(userPw: String): Future[String] = checkPattern(userPw)("userPw")
		def validName(name: String): Future[String] = checkPattern(name)("name")
		def validEmail(email: String): Future[String] =
			checkPattern(email)("email") transform  {
				case Success(email) => Try(email)
				case Failure(e)	 => Failure(e)
			} flatMap { userModel getUserByEmail(_) flatMap ({
				case None => Future successful(email)
				case Some(_) => throw new IllegalArgumentException("이미 사용중인 이메일입니다.")
			})}
		def validPhone(phone: String): Future[String] = checkPattern(phone)("phone")
		
		for {
			userId <- validUserId(user.userId)
			userPw <- validUserPw(user.userPw)
			name <- validName(user.name)
			email <- validEmail(user.email)
			phone <- validPhone(user.phonenumber)
		} yield user
	}
	
	def register(user: UserDto): Future[Option[String]] = {
		accountValidation(user) transform {
			case Success(result) =>
				userModel insertUser user
				Try(None)
			case Failure(e) => Try(Some(e.getMessage))
		}
	}
	
	def findId(email: String): Future[Option[String]] =
		userModel getUserByEmail(email) map (_ map(_.userId))
	
	def getUser(userId: String): Future[UserDto] =
		userModel getUserById(userId) map(_.getOrElse(throw new Exception()))
		
	def addCart(cart: CartDto): Future[Int] =
		cartModel addCart(cart)
		
	def addQuantity(implicit cartId: Int): Future[Int] = cartModel addQuantity
		
	def subQuantity(implicit cartId: Int): Future[Int] = cartModel subQuantity
	
	def updateQuantity(q: Int)(implicit cartId: Int): Future[Int] =
		cartModel updateQuantity(q)
	
	def getCarts(implicit userId: String): Future[List[CartDto]] =
		cartModel getCartsByUserId
		
	def getCart(implicit cartId: Int): Future[CartDto] =
		cartModel getCartByCartId
	
	def getAddress(implicit userId: String): Future[Seq[AddressDto]] =
		db.run(UserAddresses.filter(addr => addr.userId === userId).result).map(_.map(AddressDto(_)))
	
}
