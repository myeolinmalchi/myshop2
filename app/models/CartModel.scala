package models

import common.conversion.OptionList
import dto._
import java.sql.Timestamp
import javax.inject._
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

@Singleton
class CartModel @Inject() (val dbConfigProvider: DatabaseConfigProvider,
						   productModel: ProductModel)
						  (implicit ec: ExecutionContext)
	extends HasDatabaseConfigProvider[JdbcProfile] {
	
	private def cartIdQuery(cartId: Int) =  Carts.filter(_.cartId === cartId)
	
	type D[T] = DBIOAction[T, NoStream, Effect.All]
	
	def getCartsByUserId(userId: String): Future[List[CartDto]] =
		db run sql"SELECT * FROM v_carts WHERE user_id = $userId".as[CartDto] map(_.toList)
		
	def getCartByCartId(cartId: Int): Future[Option[CartDto]] =
		db run sql"SELECT * FROM v_carts WHERE cart_id = $cartId".as[CartDto].headOption
	
	def getItemIdsByCartId(cartId: Int): Future[List[Int]] =
		db run CartDetails.filter(_.cartId === cartId).map(_.optionItemId).result map(_.toList)
	
	private def getCartByCartIdQuery(implicit cartId: Int): D[Option[CartDto]] = {
		val cartException = new Exception("장바구니를 불러오지 못했습니다.")
		val itemQuery = (iid: Int) => ProductOptionItems.filter(_.productOptionItemId === iid)
		for {
			cartOption <- sql"SELECT * FROM v_carts WHERE cart_id = $cartId".as[CartDto].headOption
			finalCartOption <- DBIO.sequenceOption(cartOption map { cart =>
				for {
					details <- CartDetails.filter(_.cartId === cartId).result
					itemDtoList <- DBIO.sequence(
						details.toList map { detail =>
							for {
								itemOption <- itemQuery(detail.optionItemId).result.headOption
								item = itemOption.getOrElse(throw cartException)
							} yield ProductOptionItemDto.newInstance(item)
						}
					)
				} yield cart.setList(itemDtoList)
			})
		} yield finalCartOption
	}
	
	private def getSellerId(implicit cartId: Int): D[String] = {
		val productIdQuery = Carts.filter(_.cartId === cartId).map(_.productId)
		val sellerIdQuery = (pid: Int) => Products.filter(_.productId === pid ).map(_.sellerId)
		for {
			productIdOption <- productIdQuery.result.headOption
			productId = productIdOption.getOrElse(0)
			sellerIdOption <- sellerIdQuery(productId).result.headOption
		} yield sellerIdOption.getOrElse(throw new Exception("판매자를 찾을 수 없습니다!"))
	}
	
	private def insertOrder(userId: String): D[Int] = {
		val query = Orders returning Orders.map(_.orderId)
		val row = OrdersRow(0, userId, new Timestamp(System.currentTimeMillis()))
		query += row
	}
	
	private def insertOrderProduct(oid: Int, sid: String, cart: CartDto) = {
		val query = OrderProducts returning OrderProducts.map(_.orderProductId)
		val row = OrderProductsRow(oid, 0, cart.productId, sid, cart.quantity, "0")
		query += row
	}
	
	private def insertOrderProducts(oid: Int, carts: List[CartDto]) = DBIO.sequence {
		carts map { cart =>
			for {
				sellerId <- getSellerId(cart.cartId)
				orderProductId <- insertOrderProduct(oid, sellerId, cart)
				affected <- insertOrderProductDetails(orderProductId.self, cart.itemList)
			} yield affected.getOrElse(0)
		}
	}
	
	private def insertOrderProductDetails(pid: Int, items: List[ProductOptionItemDto]) = {
		val rows = items.map(i => OrderProductDetailsRow(pid, 0, i.productOptionItemId.get))
		OrderProductDetails ++= rows
	}

	def newOrder(userId: String, cartIdList: List[Int]): Future[Int] =
		db run (for {
			orderId <- insertOrder(userId)
			carts <- DBIO.sequence(cartIdList map (cid => getCartByCartIdQuery(cid)))
			aff1 <- DBIO.sequenceOption(
				OptionList.sequence(carts) map { carts =>
					insertOrderProducts(orderId, carts)
				}
			)
			aff2 <- DBIO.sequence(carts map updateStockWithValidation)
			aff3 <- DBIO.sequence(cartIdList map(cartIdQuery(_).delete))
		} yield aff1.getOrElse(Nil).sum + OptionList.sequence(aff2).getOrElse(Nil).sum + aff3.sum).transactionally
		
	def newOrder2(userId: String, cartIdList: List[Int]): Future[Option[Int]] =
		db run (for {
			orderId <- insertOrder(userId)
			carts <- DBIO.sequence(cartIdList map (cid => getCartByCartIdQuery(cid)))
			affOption1 <- DBIO.sequenceOption(
				OptionList.sequence(carts) map { carts =>
					insertOrderProducts(orderId, carts)
				}
			)
			affOption2 <- DBIO.sequence(carts map updateStockWithValidation)
			affOption3 <- DBIO.sequence(cartIdList map(cartIdQuery(_).delete))
		} yield for {
			aff1 <- affOption1
			aff2 <- OptionList.sequence(affOption2)
			aff3  = affOption3.sum
		} yield aff1.sum + aff2.sum + aff3).transactionally
		
	private def updateStockWithValidation(cart: Option[CartDto]): D[Option[Int]] = DBIO.sequenceOption(
		cart map { cart =>
			(for {
				checked <- productModel checkStockQuery(cart.itemList
						.map(_.productOptionItemId.get), cart.quantity)
			} yield checked match {
				case (_, true) => for {
					stockId <- productModel getStockIdQuery cart.itemList.map(_.productOptionItemId.get)
					aff <- productModel updateStockQuery(stockId, -cart.quantity)
				} yield aff
				case (stock, false) =>
					throw new Exception(s"'${cart.name}'의 재고가 부족합니다! (남은 수량: $stock)")
			}) flatten
		}
	)
	
	def insertCart(cart: CartRequestDto): Future[Int] = {
		val query = Carts.map(c => (c.userId, c.productId, c.quantity)) returning Carts.map(_.cartId)
		val row = (cart.userId, cart.productId, cart.quantity)
		
		db run(for {
			cartId <- query += row
			aff <- DBIO.sequence(
				cart.itemList map { itemId =>
					CartDetails.map(d => (d.cartId, d.optionItemId)) += (cartId, itemId)
				}
			)
		} yield aff.sum).transactionally
	}
	
	def deleteCart(implicit cartId: Int): Future[Int] =
		db run Carts.filter(_.cartId === cartId).delete
	
	def updateQuantity(q: Int, cartId: Int): Future[Int] = q match {
		case quantity if quantity > 0 =>
			db run cartIdQuery(cartId).map(_.quantity).update(quantity)
		case _ => Future.failed(new Exception("수량은 0보다 큰 값이어야 합니다."))
	}
	
}