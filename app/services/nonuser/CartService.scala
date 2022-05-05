package services.nonuser

import com.google.inject.ImplementedBy
import dto.{CartDto, CartRequestDto}
import scala.concurrent.Future

@ImplementedBy(classOf[CartServiceImpl])
trait CartService {
	
	def addQuantity(cartId: Int): Future[Int]
	def subQuantity(cartId: Int): Future[Int]
	def updateQuantity(quantity: Int, cartId: Int): Future[Int]
	def addCart(cart: CartRequestDto): Future[Int]
	def getCarts(idToken: String): Future[List[CartDto]]
	def getCart(cartId: Int): Future[CartDto]
	def deleteCart(cartId: Int): Future[Int]
	
}
