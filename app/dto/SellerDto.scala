package dto

import models.Tables._
import play.api.libs.json.Json

case class SellerDto(sellerId: String,
				   sellerPw: String,
				   name: String,
				   email: String,
				   phonenumber: String)

object SellerDto{
	
	implicit val sellerReads = Json.reads[SellerDto]
	implicit val sellerWrites = Json.writes[SellerDto]
	
	def newEntity(seller: Sellers#TableElementType) =
		new SellerDto(seller.sellerId, seller.sellerPw, seller.name, seller.email, seller.phonenumber)

	def apply(sellerId: String,
			  sellerPw: String,
			  name: String,
			  email: String,
			  phonenumber: String) =
		new SellerDto(sellerId, sellerPw, name, email, phonenumber)
}
