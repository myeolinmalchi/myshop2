package models

import scala.concurrent.{ExecutionContext, Future}
import slick.lifted.AbstractTable
import scala.concurrent.ExecutionContext
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
import play.api.libs.json.Json
import scala.language.postfixOps
import slick.lifted.AbstractTable

@Singleton
class SellerModel(db: Database)(implicit ec: ExecutionContext) {
	
	implicit val sellers = Sellers
	val common = new CommonModelApi(db)
	import common._
	
	def getSellerById(sellerId: String): Future[Option[SellerDto]] =
		selectOne[Sellers, SellerDto](SellerDto.newEntity){ seller => seller.sellerId === sellerId }
	
	def getSellerByEmail(email: String): Future[Option[SellerDto]] =
		selectOne[Sellers, SellerDto](SellerDto.newEntity){ seller => seller.email ===email }
	
	def insertSeller(seller: SellerDto): Future[Int] =
		db.run(Sellers.map(u => (u.sellerId, u.sellerPw, u.name, u.email, u.phonenumber))
				+= (seller.sellerId, SHA256.encrypt(seller.sellerPw), seller.name, seller.email, seller.phonenumber))
	
}