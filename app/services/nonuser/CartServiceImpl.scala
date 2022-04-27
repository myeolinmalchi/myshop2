package services.nonuser

import dto.CartDto
import javax.inject._
import models.{NonUserCartModel, ProductModel}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.MySQLProfile.api._

@Singleton
class CartServiceImpl @Inject() (cartModel: NonUserCartModel,
								 productModel: ProductModel)
								(implicit ec: ExecutionContext) extends CartService {
	
	def addQuantity(cartId: Int): Future[Int] = cartModel addQuantity cartId
	
	def subQuantity(cartId: Int): Future[Int] = cartModel subQuantity cartId
	
	private val outOfStockException = (stock: Int) =>
		Future.failed(new Exception(s"재고가 부족합니다! (남은 수량: ${stock})"))
	
	def updateQuantity(quantity: Int, cartId: Int): Future[Int] = {
		cartModel getCartByCartId cartId flatMap { cart =>
			println(cart.itemList.size)
			val is = cart.itemList.map(_.productOptionItemId)
			productModel checkStock(is, quantity) flatMap {
				case (stock, false) => outOfStockException(stock)
				case (_, true) => cartModel updateQuantity (quantity, cartId)
			}
		}
	}
	
	def addCart(cart: CartDto): Future[Int] = {
		val is = cart.itemList.map(_.productOptionItemId)
		productModel checkStock(is, cart.quantity) flatMap {
			case (stock, false) => outOfStockException(stock)
			case (_, true) => cartModel addCart cart
		}
	}
	
	def getCarts(idToken: String): Future[List[CartDto]] =
		cartModel getCartsByIdToken idToken
	
	def getCart(cartId: Int): Future[CartDto] =
		cartModel getCartByCartId cartId
	
	def deleteCart(cartId: Int): Future[Int] =
		cartModel deleteCart cartId
	
}
