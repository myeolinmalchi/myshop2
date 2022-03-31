package models

import common.encryption._
import dto._
import javax.inject._
import models.Tables._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.MySQLProfile.api._

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