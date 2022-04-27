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
	
	override def updateQuantity(q: Int)(implicit cartId: Int): Future[Int] =
		cartModel getItemIdsByCartId cartId flatMap { ids =>
			productModel checkStock(ids, q) flatMap {
				case (stock, false) => outOfStockException(stock)
				case (_, true) => cartModel updateQuantity q
			}
		}
	
	override def addQuantity(implicit cart: CartDto): Future[Int] =
		cartModel addQuantity cart.cartId
	
	override def subQuantity(implicit cart: CartDto): Future[Int] =
		cartModel subQuantity cart.cartId
	
	override def getCarts(implicit user: UserDto): Future[List[CartDto]] =
		cartModel getCartsByUserId user.userId.get
	
	override def getCart(implicit cart: CartDto): Future[CartDto] =
		cartModel getCartByCartId cart.cartId
	
	override def deleteCart(implicit cart: CartDto): Future[Int] =
		cartModel deleteCart cart.cartId
	
	override def updateQuantity2(cartId: Int, quantity: Int): Future[Int] = {
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
