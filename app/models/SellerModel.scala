package models

import common.encryption._
import dto._
import javax.inject._
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

@Singleton
class SellerModel @Inject() (val dbConfigProvider: DatabaseConfigProvider)
				 (implicit ec: ExecutionContext)
	extends HasDatabaseConfigProvider[JdbcProfile] {
	
	def getSellerById(sellerId: String): Future[Option[SellerDto]] =
		db run (for {
			sellerRowOption <- Sellers.filter(_.sellerId === sellerId).result.headOption
		} yield sellerRowOption.map(SellerDto.newEntity))
	
	def getSellerByEmail(email: String): Future[Option[SellerDto]] =
		db run (for {
			sellerRowOption <- Sellers.filter(_.email === email).result.headOption
		} yield sellerRowOption.map(SellerDto.newEntity))
	
	def insertSeller(seller: SellerDto): Future[Int] =
		db.run(Sellers.map(s => (s.sellerId, s.sellerPw, s.name, s.email, s.phonenumber))
				+= (seller.sellerId, SHA256.encrypt(seller.sellerPw), seller.name, seller.email, seller.phonenumber))
	
}