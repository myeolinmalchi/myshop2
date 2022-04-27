package services.seller

import common.encryption.SHA256
import dto.SellerDto
import javax.inject.Inject
import models.{ProductModel, SellerModel}
import scala.collection.mutable.Map
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class AccountServiceImpl @Inject() (sellerModel: SellerModel)
								   (implicit ec: ExecutionContext)
	extends AccountService {
	
	override def login(implicit seller: SellerDto): Future[_] =
		(sellerModel getSellerPassword seller.sellerId) flatMap {
			case Some(pw) =>
				if(pw.equals(SHA256.encrypt(seller.sellerPw))) Future.successful()
				else Future.failed(new Exception("비밀번호가 일치하지 않습니다."))
			case None => Future.failed(new Exception("존재하지 않는 계정입니다."))
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
	
	override def register(implicit seller: SellerDto): Future[_] =
		for {
			seller <- accountValidation(seller)
			aff <- sellerModel insertSeller seller
		} yield {
			if(aff == 1) ()
			else new Exception("회원가입에 실패했습니다.")
		}
	
	override def findId(email: String): Future[Option[String]] =
		sellerModel getSellerByEmail(email) map (_ map(_.sellerId))
	
	override def getSellerOption(sellerId: String): Future[Option[SellerDto]] =
		sellerModel getSellerById sellerId
		
}
