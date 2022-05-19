package models

import com.google.inject.ImplementedBy
import dto.{CartDto, CartRequestDto}
import scala.concurrent.Future

@ImplementedBy(classOf[NonUserCartModelImpl])
trait NonUserCartModel {
	def getCartsByToken(token: String): Future[List[CartDto]]
	def getCartByCartId(cartId: Int): Future[Option[CartDto]]
	def getItemIdsByCartId(cartId: Int): Future[List[Int]]
	def insertCart(cart: CartRequestDto): Future[Int]
	def deleteCart(cartId: Int): Future[Int]
	def updateQuantity(q: Int, cartId: Int): Future[Int]
}
