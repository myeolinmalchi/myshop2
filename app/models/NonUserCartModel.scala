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
class NonUserCartModel @Inject() (val dbConfigProvider: DatabaseConfigProvider,
								  productModel: ProductModel)
								 (implicit ec: ExecutionContext)
	extends HasDatabaseConfigProvider[JdbcProfile]{
	
	private def cartIdQuery(implicit cartId: Int) = NonUserCarts.filter(_.nonUserCartId === cartId)
	private def idTokenQuery(implicit idToken: String) = NonUserCarts.filter(_.idToken === idToken)
	
	private def getItemsQuery(cart: CartDto): DBIOAction[CartDto, NoStream, Effect.Read] =
		for {
			details <- NonUserCartDetails.filter(_.nonUserCartId === cart.cartId).result
			items <- DBIO.sequence(details.map { detail =>
				val innerQuery = ProductOptionItems.filter(_.productOptionItemId === detail.optionItemId.toInt)
				for {
					itemOption <- innerQuery.result.headOption
				} yield itemOption.map(ProductOptionItemDto.newInstance)
			})
			itemList = items.flatten.toList
		} yield cart.setList(itemList)
	
	private def getItems(cart: CartDto): Future[CartDto] =
		db run getItemsQuery(cart)
		
	def getCartsByIdToken(implicit idToken: String): Future[List[CartDto]] =
		db run (for {
			carts <- NonUserCarts.filter(_.idToken === idToken).result
			cartDtoList = carts.map(CartDto.newInstance2).toList
		} yield cartDtoList)
		
	def getCartByCartId(implicit cartId: Int): Future[CartDto] = {
		val cartException = new Exception("장바구니를 불러오지 못했습니다.")
		val cartQuery = NonUserCarts.filter(_.nonUserCartId === cartId)
		val detailQuery = (cid: Int) => NonUserCartDetails.filter(_.nonUserCartId === cid)
		val itemQuery = (iid: Int) => ProductOptionItems.filter(_.productOptionItemId === iid)
		
		val query = for {
			cartOption <- cartQuery.result.headOption
			cart = cartOption.getOrElse(throw cartException)
			cartDto = CartDto.newInstance2(cart)
			details <- detailQuery(cart.nonUserCartId).result
			itemDtoList <- DBIO.sequence(details.toList map { detail =>
				for {
					itemOption <- itemQuery(detail.optionItemId.toInt).result.headOption
					item = itemOption.getOrElse(throw cartException)
				} yield ProductOptionItemDto.newInstance(item)
			})
		} yield cartDto.setList(itemDtoList)
		db run query
	}
	
	
	private def insertCart(cart: CartDto)(f: CartDto => Future[Int]) = cart.itemList match {
		case Nil => Future.failed(new Exception("옵션이 비어있습니다."))
		case h::t => {
			val surcharge = (h::t).map(_.surcharge).sum
			val query = NonUserCarts returning NonUserCarts.map(_.nonUserCartId)
			val row = NonUserCartsRow(cart.userId, 0,cart.name, cart.productId,
				cart.price, cart.quantity+surcharge, new Timestamp(System.currentTimeMillis()))
			db run (query+=row) map(id => cart.setId(id)) flatMap(f(_))
		}
	}
	
	private def insertDetail(itemId: Int, cartId: Int) = {
		println(cartId)
		db run (NonUserCartDetails += NonUserCartDetailsRow(cartId, 0, itemId))
	}
	
	def addCart(cart: CartDto): Future[Int]= {
		productModel.getProductOptionsCount(cart.productId) flatMap {
			case count: Int if count == cart.itemList.size =>
				insertCart(cart) { cart =>
					cart.itemList.traverse { item =>
						insertDetail(item.productOptionItemId, cart.cartId)
					} map (_.sum)
				}
			case _ => Future.failed(throw new Exception("모든 옵션을 지정해주세요."))
		}
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
	
	def updateQuantity(q: Int, cartId: Int): Future[Int] = q match {
		case quantity if quantity > 0 =>
			db run cartIdQuery(cartId).map(_.quantity).update(quantity)
		case _ => Future.failed(new Exception("수량은 0보다 큰 값이어야 합니다."))
	}
	
}

