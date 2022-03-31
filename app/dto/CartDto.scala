package dto

import java.util.Date
import play.api.libs.json.Json
import models.Tables._

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
	def newInstance(cart: Carts#TableElementType) =
		new CartDto(cart.userId, cart.cartId, cart.name, cart.productId, cart.price, cart.quantity, cart.addedDate, Nil)
}
