package dto

import java.util.Date
import play.api.libs.json.Json
import models.Tables._

case class CartDto(userId: String,
				   cartId: Int,
				   name: String,
					productId: Int,
					price: Int,
					quantity: Int,
					addedDate: Date)

object CartDto{
	def apply(cart: Carts#TableElementType) =
		new CartDto(cart.userId, cart.cartId, cart.name, cart.productId, cart.price, cart.quantity, cart.addedDate)
}

//object ReadsAndWrites {
//	implicit val cartDtoReads = Json.reads[CartDto]
//	implicit val cartDtoWrites = Json.writes[CartDto]
//}




