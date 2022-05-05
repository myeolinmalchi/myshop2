package services.user

import cats.data.OptionT
import cats.implicits.toTraverseOps
import common.validation.ValidationResultLib
import dto.{CartDto, CartRequestDto}
import javax.inject.{Inject, Singleton}
import models.{CartModel, ProductModel}
import play.api.mvc.Result
import play.api.mvc.Results.{Forbidden, NotFound}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import services.user.CartService._

@Singleton
class CartServiceImpl @Inject() (cartModel: CartModel,
								 productModel: ProductModel)
								(implicit ec: ExecutionContext) extends CartService with ValidationResultLib[Future]{
	
	
	private val outOfStockException = (stock: Int) =>
		Future.failed(new Exception(s"재고가 부족합니다! (남은 수량: ${stock})"))
	
	override def updateQuantity(cartId: Int, quantity: Int): Future[Int] = {
		implicit val id: Int = cartId
		cartModel getItemIdsByCartId cartId flatMap { ids =>
			productModel checkStock(ids, quantity) flatMap {
				case (stock, false) => outOfStockException(stock)
				case (_, true) => cartModel updateQuantity quantity
			}
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
		
	private def checkCorrectProductId(itemId: Int, productId: Int): Future[Boolean] =
		productModel getProductIdByItemId itemId map {
			case Some(pid) if pid == productId => true
			case Some(_) => false
			case None => false
		}
		
	private def checkAllCorrectProductId(cart: CartRequestDto): ValidationResult[CartInsertionFailure, Unit] =
		ValidationResult.ensureM(
			cart.itemList traverse { itemId =>
				checkCorrectProductId(itemId, cart.productId)
			} map(_.forall(is => is)),
			onFailure = IncorrectProductId
		)
		
	private def checkStock(implicit cart: CartRequestDto): ValidationResult[CartInsertionFailure, Unit] =
		ValidationResult(
			productModel getStockByItemList cart.itemList flatMap { stock =>
				ValidationResult.ensure(
					stock >= cart.quantity,
					onFailure = OutOfStock(stock)
				).value
			}
		)
		
	override def addCart(implicit cart: CartRequestDto): Future[Either[CartInsertionFailure, Int]] =
		(for {
			_ <- checkAllCorrectProductId(cart)
			_ <- checkStock
		} yield ()) onSuccess(cartModel insertCart cart)
	
}
