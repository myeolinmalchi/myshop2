package services

import common.encryption.SHA256.encrypt
import dto._
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
import models.{CommonModelApi, ProductModel}
import play.api.libs.json.Json
import scala.language.postfixOps
import slick.lifted.AbstractTable

/**
 * 판매자별 상품 CRUD 및 판매자 계정 CRUD
 * @author minsuk
 * @version 1.0.0
 * 작성일 2022-03-17
 **/
@Singleton
class SellerService(db: Database)(implicit ec: ExecutionContext) {
	
	implicit val sellers = Sellers
	implicit val products = Products
	implicit val options = ProductOptions
	implicit val items = ProductOptionItems
	
	val model = new ProductModel(db)
	val commonApi = new CommonModelApi(db)
	import commonApi._
	
//	TODO
//	 - Service 레이어에서는 db run에 직접 접근하지 않게끔 수정
	def login(sellerId: String, sellerPw: String): Future[Option[Boolean]] = {
		import SHA256._
		db.run(Sellers.filter(u => u.sellerId === sellerId).result).map(_.headOption match {
			case Some(u) =>
				if (u.sellerPw.equals(encrypt(sellerPw))) Some(true) // 로그인 성공
				else Some(false) // 비밀번호 불일치
			case None => None // 존재하지 않는 계정
		})
	}
	
	private def validate(seller: SellerDto): Future[SellerDto] = {
		def checkPattern(str: String, pattern: String, errMsg: String): Future[String] =
			if (!str.matches(pattern)) {
				Future.failed(new IllegalArgumentException(errMsg))
			} else Future.successful(str)
		
		def validSellerId(sellerId: String): Future[String] = {
			val pattern = "^[a-z]+[a-z0-9]{5,19}$"
			if (!sellerId.matches(pattern)) {
				Future.failed(new IllegalArgumentException("유효하지 않은 아이디입니다."))
			} else {
//				 TODO
				val matches = db.run(Sellers.filter(u => u.sellerId === sellerId).result)
				matches.flatMap { sellerRows =>
					if (sellerRows.isEmpty) Future.successful(sellerId)
					else throw new IllegalArgumentException("이미 존재하는 계정입니다.")
				}
			}
		}
		
		def validSellerPw(sellerPw: String): Future[String] =
			checkPattern(sellerPw,
				"^(?=.*\\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,16}$",
				"유효하지 않은 비밀번호입니다.")
		
		def validName(name: String): Future[String] =
			checkPattern(name,
				"^[ㄱ-힣]+$",
				"유효하지 않은 이름입니다.")
		
		def validEmail(email: String): Future[String] = {
			val pattern = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$"
			if (!email.matches(pattern)) {
				Future.failed(new IllegalArgumentException("유효하지 않은 이메일입니다."))
			} else {
//				 TODO
				val matches = db.run(Sellers.filter(u => u.email === email).result)
				matches.flatMap { sellerRows =>
					if (sellerRows.isEmpty) Future.successful(email)
					else throw new IllegalArgumentException("이미 존재하는 이메일입니다.")
				}
			}
		}
		
		def validPhone(phone: String): Future[String] =
			checkPattern(phone,
				"^\\d{3}-\\d{3,4}-\\d{4}$",
				"유효하지 않은 전화번호입니다.")
		
		for {
			sellerId <- validSellerId(seller.sellerId)
			sellerPw <- validSellerPw(seller.sellerPw)
			name <- validName(seller.name)
			email <- validEmail(seller.email)
			phone <- validPhone(seller.phonenumber)
		} yield seller
	}
	
	def insertSeller(seller: SellerDto) = {
		import SHA256._
		db.run(Sellers.map(u => (u.sellerId, u.sellerPw, u.name, u.email, u.phonenumber))
				+= (seller.sellerId, encrypt(seller.sellerPw), seller.name, seller.email, seller.phonenumber))
	}
	
	def register(seller: SellerDto): Future[Option[String]] = {
		import SHA256._
		validate(seller).transform {
			case Success(result) =>
				insertSeller(seller)
				Try(None)
			case Failure(e) => Try(Some(e.getMessage))
		}
	}
	
//	 TODO
	def findId(email: String): Future[Option[String]] =
		selectOne[Sellers, SellerDto](SellerDto.newEntity){ seller =>
			seller.email === email } map (_ map(_.sellerId))
		
	def getProductList(implicit sellerId: String): Future[List[ProductDto]] =
		model.getProductList(product => product.sellerId === sellerId)
	
	def addProduct(implicit sellerId: String, p: ProductDto): Future[_] = model.addProduct
	
}
