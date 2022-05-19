package services.nonuser

import com.google.inject.ImplementedBy
import common.validation.CartValidationLib
import common.validation.CartValidationLib.CartInsertionFailure
import dto.{CartDto, CartRequestDto}
import scala.concurrent.Future

@ImplementedBy(classOf[CartServiceImpl])
trait CartService extends CartValidationLib{
	
	def updateQuantity(quantity: Int, cartId: Int): Future[Int]
	def addCart(cart: CartRequestDto): Future[Either[CartInsertionFailure, Int]]
	def getCarts(idToken: String): Future[List[CartDto]]
	def getCart(cartId: Int): Future[Option[CartDto]]
	def deleteCart(cartId: Int): Future[Int]
	
}
