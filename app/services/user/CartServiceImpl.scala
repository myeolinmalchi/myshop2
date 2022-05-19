package services.user

import common.validation.CartValidationLib.CartInsertionFailure
import common.validation.{CartValidationLib, ValidationResultLib}
import dto.{CartDto, CartRequestDto}
import javax.inject.{Inject, Singleton}
import models.{CartModel, ProductModel}
import play.api.mvc.Result
import play.api.mvc.Results.{Forbidden, NotFound}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class CartServiceImpl @Inject()(cartModel: CartModel)
															 (implicit ec: ExecutionContext,
																productModel: ProductModel)
	extends CartService with CartValidationLib {
	
	private val outOfStockException = (stock: Int) =>
		Future.failed(new Exception(s"재고가 부족합니다! (남은 수량: ${stock})"))
	
	override def updateQuantity(cartId: Int, quantity: Int): Future[Int] =
		cartModel getItemIdsByCartId cartId flatMap { ids =>
			productModel checkStock(ids, quantity) flatMap {
				case (stock, false) => outOfStockException(stock)
				case (_, true) => cartModel updateQuantity(quantity, cartId)
			}
		}
	
	override def getCarts(userId: String): Future[List[CartDto]] =
		cartModel getCartsByUserId userId
	
	override def getCart(cartId: Int): Future[Option[CartDto]] =
		cartModel getCartByCartId cartId
	
	override def deleteCart(cartId: Int): Future[Int] =
		cartModel deleteCart cartId
	
	override def checkOwnCart(userId: String, cartId: Int)
													 (result: => Future[Result]): Future[Result] =
		getCart(cartId) flatMap {
			case Some(cart) if cart.userId == userId => result
			case Some(_) => Future(Forbidden)
			case None => Future(NotFound)
		}
	
	override def addCart(cart: CartRequestDto): Future[Either[CartInsertionFailure, Int]] =
		(for {
			_ <- checkProductExists(cart)
			_ <- checkOptionCount(cart)
			_ <- checkAllCorrectProductId(cart)
			_ <- checkStock(cart)
		} yield ()) onSuccess (cartModel insertCart cart)
	
}
