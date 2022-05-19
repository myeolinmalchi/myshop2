package services.user

import com.google.inject.ImplementedBy
import common.validation.CartValidationLib
import common.validation.CartValidationLib.CartInsertionFailure
import common.validation.ValidationResultLib.ValidationFailure
import dto.{CartDto, CartRequestDto}
import play.api.mvc.Result
import scala.concurrent.Future

@ImplementedBy(classOf[CartServiceImpl])
trait CartService extends CartValidationLib {
	
	def addCart(cart: CartRequestDto): Future[Either[CartInsertionFailure, Int]]
	
	def updateQuantity(cartId: Int, quantity: Int): Future[Int]
	
	def getCarts(userId: String): Future[List[CartDto]]
	
	def getCart(cartId: Int): Future[Option[CartDto]]
	
	def deleteCart(cartId: Int): Future[Int]
	
	def checkOwnCart(userId: String, cartId: Int)
									(result: => Future[Result]): Future[Result]
	
}
