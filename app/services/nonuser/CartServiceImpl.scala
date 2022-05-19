package services.nonuser

import common.validation.CartValidationLib
import common.validation.CartValidationLib.CartInsertionFailure
import dto.{CartDto, CartRequestDto}
import javax.inject._
import models.{NonUserCartModel, ProductModel}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class CartServiceImpl @Inject()(cartModel: NonUserCartModel)
															 (implicit ec: ExecutionContext,
																productModel: ProductModel)
	extends CartService
		with CartValidationLib {
	
	private val outOfStockException = (stock: Int) =>
		Future.failed(new Exception(s"재고가 부족합니다! (남은 수량: ${stock})"))
	
	def updateQuantity(quantity: Int, cartId: Int): Future[Int] = {
		cartModel getItemIdsByCartId cartId flatMap { ids =>
			productModel checkStock(ids, quantity) flatMap {
				case (stock, false) => outOfStockException(stock)
				case (_, false) => cartModel updateQuantity(quantity, cartId)
			}
		}
	}
	
	def addCart(cart: CartRequestDto): Future[Either[CartInsertionFailure, Int]] =
		(for {
			_ <- checkProductExists(cart)
			_ <- checkOptionCount(cart)
			_ <- checkAllCorrectProductId(cart)
			_ <- checkStock(cart)
		} yield ()) onSuccess (cartModel insertCart cart)
	
	
	def getCarts(idToken: String): Future[List[CartDto]] =
		cartModel getCartsByToken idToken
	
	def getCart(cartId: Int): Future[Option[CartDto]] =
		cartModel getCartByCartId cartId
	
	def deleteCart(cartId: Int): Future[Int] =
		cartModel deleteCart cartId
	
}
