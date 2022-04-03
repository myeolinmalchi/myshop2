package dto

import java.util.Date
import models.Tables._
import play.api.libs.json.Json

case class CartDto(userId: String,
				   var cartId: Int,
				   name: String,
				   productId: Int,
				   price: Int,
				   quantity: Int,
				   addedDate: Date,
				   var itemList: List[ProductOptionItemDto]=Nil) {
	def setList(itemList: List[ProductOptionItemDto]): CartDto = {
		this.itemList = itemList
		this
	}
	
	def setId(id: Int) = {
		this.cartId = id
		this
	}
}

object CartDto{
	
	implicit val itemWrites = Json.writes[ProductOptionItemDto]
	implicit val itemReads = Json.reads[ProductOptionItemDto]

	implicit val cartWrites = Json.writes[CartDto]
	implicit val cartReads = Json.reads[CartDto]
	
	def newInstance(cart: Carts#TableElementType) =
		new CartDto(cart.userId, cart.cartId, cart.name, cart.productId, cart.price, cart.quantity, new Date(), Nil)
		
	def newInstance2(cart: NonUserCarts#TableElementType) =
		new CartDto(cart.idToken, cart.nonUserCartId, cart.name, cart.productId, cart.price, cart.quantity, new Date(), Nil)
}
