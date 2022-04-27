package models

import cats.implicits.toTraverseOps
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
	
	private def cartIdQuery(implicit cartId: Int) =  Carts.filter(_.cartId === cartId)
	private def userIdQuery(implicit userId: String) = Carts.filter(_.userId === userId)
	
	type D[T] = DBIOAction[T, NoStream, Effect.All]
	
	private def getItemsQuery(cart: CartDto): DBIOAction[CartDto,NoStream,Effect.All] =
		for {
			details <- CartDetails.filter(_.cartId === cart.cartId).result
			items <- DBIO.sequence(details.map { detail =>
				val innerQuery = ProductOptionItems.filter(_.productOptionItemId === detail.optionItemId)
				for {
					itemOption <- innerQuery.result.headOption
				} yield itemOption.map(ProductOptionItemDto.newInstance)
			})
			itemList = items.flatten.toList
		} yield cart.setList(itemList)
	
	private def getItems(cart: CartDto): Future[CartDto] =
		db run getItemsQuery(cart)
	
	def getCartsByUserId(implicit userId: String): Future[List[CartDto]] =
		db run (for {
			carts <- Carts.filter(_.userId === userId).result
			cartDtoList <- DBIO.sequence(carts.map(CartDto.newInstance).map(getItemsQuery).toList)
		} yield cartDtoList)
		
	def getCartByCartId(implicit cartId: Int): Future[CartDto] =
		db run getCartByCartIdQuery
		
	def getItemIdsByCartId(implicit cartId: Int): Future[List[Int]] =
		db run CartDetails.filter(_.cartId === cartId).map(_.optionItemId).result map(_.toList)
	
	private def getCartByCartIdQuery(implicit cartId: Int): D[CartDto] = {
		val cartException = new Exception("장바구니를 불러오지 못했습니다.")
		val cartQuery = Carts.filter(_.cartId === cartId)
		val detailQuery = (cid: Int) => CartDetails.filter(_.cartId === cid)
		val itemQuery = (iid: Int) => ProductOptionItems.filter(_.productOptionItemId === iid)
		
		for {
			cartOption <- cartQuery.result.headOption
			cart = cartOption.getOrElse(throw cartException)
			cartDto = CartDto.newInstance(cart)
			details <- detailQuery(cart.cartId).result
			itemDtoList <- DBIO.sequence(details.toList map { detail =>
				for {
					itemOption <- itemQuery(detail.optionItemId).result.headOption
					item = itemOption.getOrElse(throw cartException)
				} yield ProductOptionItemDto.newInstance(item)
			})
		} yield cartDto.setList(itemDtoList)
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
		val rows = items.map(i => OrderProductDetailsRow(pid, 0, i.productOptionItemId))
		OrderProductDetails ++= rows
	}

	def newOrder(userId: String, cartIdList: List[Int]): Future[Int] =
		db run (for {
			orderId <- insertOrder(userId)
			carts <- DBIO.sequence(cartIdList map (cid => getCartByCartIdQuery(cid)))
			aff1 <- insertOrderProducts(orderId.self,  carts)
			aff2 <- DBIO.sequence(carts map updateStockWithValidation)
			aff3 <- DBIO.sequence(cartIdList map(implicit id => cartIdQuery.delete))
		} yield aff1.sum + aff2.sum + aff3.sum).transactionally
		
	def updateStockWithValidation(cart: CartDto): D[Int] =
		(for {
			checked <- productModel checkStockQuery(cart.itemList
					.map(_.productOptionItemId), cart.quantity)
		} yield checked match {
			case (_, true) => for {
				stockId <- productModel getStockIdQuery cart.itemList.map(_.productOptionItemId)
				aff <- productModel updateStockQuery(stockId, -cart.quantity)
			} yield aff
			case (stock, false) =>
				throw new Exception(s"'${cart.name}'의 재고가 부족합니다! (남은 수량: ${stock})")
		}) flatten
	
	private def insertCart(cart: CartDto) = cart.itemList match {
		case h::t =>
			val surcharge = (h::t).map(_.surcharge).sum
			val query = Carts returning Carts.map(_.cartId)
			val row = CartsRow(cart.userId, 0,cart.name, cart.productId,
				cart.price, cart.quantity+surcharge, new Timestamp(System.currentTimeMillis()))
			query += row
	}
	
	private def itemToRow(cartId: Int, item: ProductOptionItemDto) =
		CartDetailsRow(0, cartId, item.productOptionItemId)
		
	def addCart(cart: CartDto): Future[Int]=
		productModel getProductOptionsCount cart.productId flatMap {
			case count if count == cart.itemList.size =>
				db run (for {
					cartId <- insertCart(cart)
					itemResult =  cart.itemList.map(itemToRow(cartId.self, _))
					affected <- CartDetails ++= itemResult
				} yield affected.getOrElse(0)).transactionally
			case _ => Future.failed(new Exception("옵션을 모두 선택하세요."))
		}
	
	def deleteCart(implicit cartId: Int): Future[Int] =
		db run cartIdQuery.delete
		
	def addQuantity(implicit cartId: Int): Future[Int] = {
		val temp = cartIdQuery
		val query = for {
			quantityOption <- temp.map(_.quantity).result.headOption
			updateOption = quantityOption.map(quantity => temp.map(_.quantity).update(quantity+1))
			affected <- updateOption.getOrElse(DBIO.successful(0))
		} yield affected
		db run query
	}
	
	def subQuantity(implicit cartId: Int): Future[Int] = {
		val temp = cartIdQuery
		val query =  for {
			quantityOption <- temp.map(_.quantity).result.headOption
			updateOption = quantityOption.map {
				case quantity if quantity > 1 =>
					temp.map(_.quantity).update(quantity - 1)
				case _ => DBIO.successful(0)
			}
			affected <- updateOption.getOrElse(DBIO.successful(0))
		} yield affected
		db run query
	}
	
	def updateQuantity(q: Int)(implicit cartId: Int): Future[Int] = q match {
		case quantity if quantity > 0 =>
			db run cartIdQuery.map(_.quantity).update(quantity)
		case _ => Future.failed(new Exception("수량은 0보다 큰 값이어야 합니다."))
	}
}