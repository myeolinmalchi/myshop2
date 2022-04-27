package services

import common.encryption._
import dto._
import javax.inject._
import models.Tables._
import models.{CartModel, OrderModel, ProductModel, ReviewModel, UserModel}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

/**
 * user 테이블 및 user_id를 외래키로 갖는 테이블의 데이터를 다루는 클래스
 * @author minsu
 * @version 1.0.0
 * 작성일 2022-03-17
 **/
@Singleton
class UserServiceImpl @Inject() (cartModel: CartModel,
								 productModel: ProductModel,
								 orderModel: OrderModel,
								 userModel: UserModel,
								 reviewModel: ReviewModel)
								(implicit ec: ExecutionContext) extends UserService {
	
	def login(userId: String, userPw: String): Future[_] =
		(userModel getUserPassword userId) flatMap {
			case Some(pw) =>
				if (pw.equals(SHA256.encrypt(userPw))) Future.successful()
				else Future.failed(new Exception("비밀번호가 일치하지 않습니다"))
			case None => Future.failed(new Exception("존재하지 않는 계정입니다."))
		}
	
	def accountValidation(user: UserDto): Future[UserDto] = {
		import UserDto._
		
		def checkPattern(key: String)(implicit str: String): Future[String] =
			if (!str.matches(PATTERNS(key))) Future.failed(NONE_MATCH_EXCEPTION(key))
			else Future.successful(str)
			
		def validUserId(implicit userId: String): Future[String] =
			for {
				patternCheck <- checkPattern(USER_ID)
				existenceCheck <- userModel checkUserExist patternCheck
			} yield existenceCheck match {
				case None => userId
				case Some(_) => throw OVERLAP_EXCEPTION(USER_ID)
			}
			
		def validUserPw(implicit userPw: String): Future[String] = checkPattern(USER_PW)
		
		def validName(implicit name: String): Future[String] = checkPattern(NAME)
		
		def validEmail(implicit email: String): Future[String] =
			for {
				patternCheck <-checkPattern(EMAIL)
				existenceCheck <- userModel checkEmailExist patternCheck
			} yield existenceCheck match {
				case None => email
				case Some(_) => throw OVERLAP_EXCEPTION(EMAIL)
			}
			
		def validPhone(implicit phone: String): Future[String] = checkPattern(PHONE)
		
		for {
			userId <- validUserId(user.userId.get)
			userPw <- validUserPw(user.userPw.get)
			name <- validName(user.name.get)
			email <- validEmail(user.email.get)
			phone <- validPhone(user.phonenumber.get)
		} yield user
	}
	
	def register(user: UserDto): Future[_] =
		for {
			user <- accountValidation(user)
			aff <- userModel insertUser user
		} yield {
			if(aff == 1) ()
			else new Exception("회원가입에 실패했습니다.")
		}
	
	def findId(email: String): Future[Option[String]] =
		userModel getUserByEmail(email) map (_ flatMap(_.userId))
	
	def getUser(userId: String): Future[UserDto] =
		userModel getUserById(userId) map(_.getOrElse(throw new Exception()))
	
	def getUserOption(userId: String): Future[Option[UserDto]] =
		userModel getUserById userId
	
	private val outOfStockException = (stock: Int) =>
		Future.failed(new Exception(s"재고가 부족합니다! (남은 수량: ${stock})"))
	
	def updateQuantity(q: Int)(implicit cartId: Int): Future[Int] =
		cartModel getItemIdsByCartId cartId flatMap { ids =>
			productModel checkStock(ids, q) flatMap {
				case (stock, false) => outOfStockException(stock)
				case (_, true) => cartModel updateQuantity q
			}
		}
	
	def addCart(cart: CartDto): Future[Int] = {
		val is = cart.itemList.map(_.productOptionItemId)
		productModel checkStock(is, cart.quantity) flatMap {
			case (stock, false) => outOfStockException(stock)
			case (_, true) => cartModel addCart cart
		}
	}
	
	def newOrder(userId: String, cartIdList: List[Int]): Future[Int] =
		cartModel newOrder(userId, cartIdList)
		
	def getOrderByUserId(userId: String): Future[List[OrderDto]] =
		orderModel getOrdersByUserId(userId)
	
	def addQuantity(implicit cartId: Int): Future[Int] = cartModel addQuantity
		
	def subQuantity(implicit cartId: Int): Future[Int] = cartModel subQuantity
	
	def getCarts(implicit userId: String): Future[List[CartDto]] =
		cartModel getCartsByUserId
		
	def getCart(implicit cartId: Int): Future[CartDto] =
		cartModel getCartByCartId
		
	def deleteCart(implicit cartId: Int): Future[Int] =
		cartModel deleteCart
	
//	def getAddress(implicit userId: String): Future[Seq[AddressDto]] =
//		db.run(UserAddresses.filter(addr => addr.userId === userId).result).map(_.map(AddressDto(_)))
	
	def checkUserOrderedThisProduct(userId: String, productId: Int): Future[_] =
		orderModel checkUserOrderedThisProduct (userId, productId)
		
	def insertReview(review: ReviewDto): Future[Int] =
		reviewModel insertReview review
		
	def getReviews(userId: String): Future[List[ReviewDto]] =
		reviewModel getReviewsByUserId userId
	
}
