package dto

import java.util.Date
import models.Tables._
import play.api.libs.json.Json
import slick.jdbc.GetResult

case class CartDto(userId: String,
				   cartId: Int,
				   name: String,
				   productId: Int,
				   price: Int,
				   quantity: Int,
				   addedDate: Date,
				   itemList: List[ProductOptionItemDto]=Nil) {
	
	def setList(itemList: List[ProductOptionItemDto]): CartDto =
		new CartDto(
			userId, cartId, name, productId, price, quantity, addedDate, itemList
		)
	
	def setCartId(cartId: Int): CartDto =
		new CartDto(
			userId, cartId, name, productId, price, quantity, addedDate, itemList
		)
	
}

case class CartRequestDto(userId: String,
						  cartId: Option[Int],
						  name: Option[String],
						  productId: Int,
						  price: Option[Int],
						  quantity: Int,
						  addedDate: Option[Date],
						  itemList: List[Int]=Nil) {
	def setUserId(userId: String): CartRequestDto =
		CartRequestDto(userId, cartId, name, productId, price, quantity, addedDate, itemList)
}

object CartDto {
	
	implicit val itemWrites = Json.writes[ProductOptionItemDto]
	implicit val itemReads = Json.reads[ProductOptionItemDto]

	implicit val cartWrites = Json.writes[CartDto]
	implicit val cartReads = Json.reads[CartDto]
	
	implicit val getCartResult = GetResult { r =>
		CartDto(r.nextString(), r.nextInt(), r.nextString(), r.nextInt(), r.nextInt(), r.nextInt(), r.nextDate())
	}
	
	def newInstance2(cart: NonUserCarts#TableElementType) =
		new CartDto(cart.idToken, cart.nonUserCartId, cart.name, cart.productId, cart.price, cart.quantity, new Date(), Nil)
		
}

object CartRequestDto {
	implicit val cartWrites = Json.writes[CartRequestDto]
	implicit val cartReads = Json.reads[CartRequestDto]
}