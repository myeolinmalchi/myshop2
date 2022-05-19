package services

import dto.{CartDto, CartRequestDto}
import javax.inject._
import models.{NonUserCartModelImpl, ProductModel}
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
class NonUserService @Inject() (cartModel: NonUserCartModelImpl,
																productModel: ProductModel)
							   (implicit ec: ExecutionContext) {
	
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
	
	def addCart(cart: CartRequestDto): Future[Int] = {
		productModel checkStock(cart.itemList, cart.quantity) flatMap {
			case (stock, false) => outOfStockException(stock)
			case (_, true) => cartModel insertCart cart
		}
	}
	
	def getCarts(implicit idToken: String): Future[List[CartDto]] =
		cartModel getCartsByToken idToken
	
	def getCart(implicit cartId: Int): Future[CartDto] =
		cartModel.getCartByCartId(cartId).map(_.getOrElse(throw new Exception))
	
	def deleteCart(implicit cartId: Int): Future[Int] =
		cartModel deleteCart cartId
	
}
