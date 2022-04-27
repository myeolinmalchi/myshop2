package services

import dto.CartDto
import javax.inject._
import models.{NonUserCartModel, ProductModel}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.MySQLProfile.api._

/**
 * user 테이블 및 user_id를 외래키로 갖는 테이블의 데이터를 다루는 클래스
 * @author minsu
 * @version 1.0.0
 * 작성일 2022-03-17
 **/
@Singleton
class NonUserService @Inject() (cartModel: NonUserCartModel,
								productModel: ProductModel)
							   (implicit ec: ExecutionContext) {
	
	def addQuantity(implicit cartId: Int): Future[Int] = cartModel addQuantity
	
	def subQuantity(implicit cartId: Int): Future[Int] = cartModel subQuantity
	
	private val outOfStockException = (stock: Int) =>
		Future.failed(new Exception(s"재고가 부족합니다! (남은 수량: ${stock})"))
	
	def updateQuantity(q: Int)(implicit cartId: Int): Future[Int] = {
		cartModel getCartByCartId cartId flatMap { cart =>
			println(cart.itemList.size)
			val is = cart.itemList.map(_.productOptionItemId)
			productModel checkStock(is, q) flatMap {
				case (stock, false) => outOfStockException(stock)
				case (_, true) => cartModel updateQuantity (q, cartId)
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
	
	def getCarts(implicit idToken: String): Future[List[CartDto]] =
		cartModel getCartsByIdToken
	
	def getCart(implicit cartId: Int): Future[CartDto] =
		cartModel getCartByCartId
	
	def deleteCart(implicit cartId: Int): Future[Int] =
		cartModel deleteCart
	
}
