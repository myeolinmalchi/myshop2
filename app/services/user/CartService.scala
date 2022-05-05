package services.user

import cats.data.OptionT
import com.google.inject.ImplementedBy
import common.validation.ValidationResultLib
import dto.{CartDto, CartRequestDto, UserRequestDto}
import play.api.data.validation.ValidationResult
import play.api.mvc.Result
import scala.concurrent.Future
import services.user.CartService.CartInsertionFailure

@ImplementedBy(classOf[CartServiceImpl])
trait CartService extends ValidationResultLib[Future]{
	
	def addCart(implicit cart: CartRequestDto): Future[Either[CartInsertionFailure, Int]]
	def updateQuantity(cartId: Int, quantity: Int): Future[Int]
	def getCarts(userId: String): Future[List[CartDto]]
	def getCart(cartId: Int): Future[Option[CartDto]]
	def deleteCart(cartId: Int): Future[Int]
	def checkOwnCart(userId: String, cartId: Int)
					(result: => Future[Result]): Future[Result]
	
}

object CartService {
	sealed abstract class CartInsertionFailure
	case class OutOfStock(stock: Int) extends CartInsertionFailure
	case object IncorrectProductId extends CartInsertionFailure
}
