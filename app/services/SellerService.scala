package services

import common.encryption._
import dto._
import javax.inject._
import models.{ProductModel, SellerModel}
import scala.collection.mutable.Map
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import slick.jdbc.MySQLProfile.api._

/**
 * 판매자별 상품 CRUD 및 판매자 계정 CRUD
 * @author minsuk
 * @version 1.0.0
 * 작성일 2022-03-17
 **/
@Singleton
class SellerService(db: Database)(implicit ec: ExecutionContext) {
	
	val productModel = new ProductModel(db)
	val sellerModel = new SellerModel(db)
	
	def login(sellerId: String, sellerPw: String): Future[Option[Boolean]] =
		sellerModel.getSellerById(sellerId) map {
			case Some(u) =>
				if (u.sellerPw.equals(SHA256.encrypt(sellerPw))) Some(true) // 로그인 성공
				else Some(false) // 비밀번호 불일치
			case None => None // 존재하지 않는 계정
		}
	
	def accountValidation(seller: SellerDto): Future[SellerDto] = {
		def patterns(implicit key: String) = Map (
			"sellerId" -> "^[a-z]+[a-z0-9]{5,19}$",
			"sellerPw" -> "^(?=.*\\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,16}$",
			"name" -> "^[ㄱ-힣]+$",
			"email" -> "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$",
			"phone" -> "^\\d{3}-\\d{3,4}-\\d{4}$"
		)(key)
		def noneMatchedMsg(implicit key: String): String = Map (
			"sellerId" -> "유효하지 않은 아이디입니다.",
			"sellerPw" -> "유효하지 않은 비밀번호입니다.",
			"name"  -> "유효하지 않은 이름입니다.",
			"email" -> "유효하지 않은 이메일입니다.",
			"phone" -> "유효하지 않은 전화번호입니다."
		)(key)
		def checkPattern(str: String)(implicit key: String): Future[String] =
			if (!str.matches(patterns))
				Future.failed(new IllegalArgumentException(noneMatchedMsg))
			else Future.successful(str)
		
		def validSellerId(sellerId: String): Future[String] =
			checkPattern(sellerId)("sellerId") transform  {
				case Success(sellerId) => Try(sellerId)
				case Failure(e)	 => Failure(e)
			} flatMap { sellerModel getSellerById(_) flatMap ({
				case None => Future.successful(sellerId)
				case Some(_) => throw new IllegalArgumentException("이미 존재하는 계정입니다.")
			})}
		def validSellerPw(sellerPw: String): Future[String] = checkPattern(sellerPw)("sellerPw")
		def validName(name: String): Future[String] = checkPattern(name)("name")
		def validEmail(email: String): Future[String] =
			checkPattern(email)("email") transform  {
				case Success(email) => Try(email)
				case Failure(e)	 => Failure(e)
			} flatMap { sellerModel getSellerByEmail(_) flatMap ({
				case None => Future successful(email)
				case Some(_) => throw new IllegalArgumentException("이미 사용중인 이메일입니다.")
			})}
		def validPhone(phone: String): Future[String] = checkPattern(phone)("phone")
		
		for {
			sellerId <- validSellerId(seller.sellerId)
			sellerPw <- validSellerPw(seller.sellerPw)
			name <- validName(seller.name)
			email <- validEmail(seller.email)
			phone <- validPhone(seller.phonenumber)
		} yield seller
	}
	
	def register(seller: SellerDto): Future[Option[String]] = {
		accountValidation(seller) transform {
			case Success(result) =>
				sellerModel insertSeller seller
				Try(None)
			case Failure(e) => Try(Some(e.getMessage))
		}
	}
	
	def findId(email: String): Future[Option[String]] =
		sellerModel getSellerByEmail(email) map (_ map(_.sellerId))
		
	def getSeller(sellerId: String): Future[SellerDto] =
		sellerModel getSellerById(sellerId) map(_.getOrElse(throw new Exception()))
		
	def getProductList(implicit sellerId: String): Future[List[ProductDto]] =
		productModel.getProductsWithAll(product => product.sellerId === sellerId)
	
	def addProduct(sellerId: String, p: ProductDto): Future[_] =
		productModel.insertProductWithAll(p) flatMap productModel.insertProductStock
	
	def searchProducts(keyword: String): Future[List[ProductDto]] =
		productModel getProductsWithAll { product => product.name like s"%${keyword}%" }
	
	def getProductStock(productId: Int): Future[List[StockResponseDto]] =
		productModel getProductStock(productId)
		
	def updateStock(stockId: Int, adds: Int): Future[Int] =
		productModel updateStock(stockId, adds)
	
}
