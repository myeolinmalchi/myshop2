package services.user

import dto.{CartDto, UserDto}
import javax.inject.{Inject, Singleton}
import models.{CartModel, ProductModel}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class CartServiceImpl @Inject() (cartModel: CartModel,
								 productModel: ProductModel)
								(implicit ec: ExecutionContext) extends CartService {
	
	
	private val outOfStockException = (stock: Int) =>
		Future.failed(new Exception(s"재고가 부족합니다! (남은 수량: ${stock})"))
	
	override def addCart(implicit cart: CartDto): Future[Int] = {
		val is = cart.itemList.map(_.productOptionItemId)
		productModel checkStock(is, cart.quantity) flatMap {
			case (stock, false) => outOfStockException(stock)
			case (_, true) => cartModel addCart cart
		}
	}
	
	override def updateQuantity(cartId: Int, quantity: Int): Future[Int] = {
		implicit val id: Int = cartId
		cartModel getItemIdsByCartId cartId flatMap { ids =>
			productModel checkStock(ids, quantity) flatMap {
				case (stock, false) => outOfStockException(stock)
				case (_, true) => cartModel updateQuantity quantity
			}
		}
	}
	
	override def addQuantity(cartId: Int): Future[Int] =
		cartModel addQuantity cartId
	
	override def subQuantity(cartId: Int): Future[Int] =
		cartModel subQuantity cartId
	
	override def getCarts(userId: String): Future[List[CartDto]] =
		cartModel getCartsByUserId userId
	
	override def getCart(cartId: Int): Future[CartDto] =
		cartModel getCartByCartId cartId
	
	override def deleteCart(cartId: Int): Future[Int] =
		cartModel deleteCart cartId
		
	override def checkOwnCart(userId: String, cartId: Int): Future[Boolean] =
		cartModel getCartByCartId cartId map { cart =>
			cart.userId == userId
		}
}
