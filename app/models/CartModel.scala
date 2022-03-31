package models

import cats.data.Chain.catsDataTraverseFilterForChain.traverse
import cats.implicits.catsStdTraverseFilterForList.traverse
import cats.implicits.toTraverseOps
import common.encryption._
import dto._
import javax.inject._
import models.Tables._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.MySQLProfile.api._

@Singleton
class CartModel(db: Database)(implicit ec: ExecutionContext) {
	
	implicit val carts = Carts
	val common = new CommonModelApi(db)
	val productModel = new ProductModel(db)
	
	private def cartIdQuery(implicit cartId: Int) =  Carts.filter(_.cartId === cartId)
	private def userIdQuery(implicit userId: String) = Carts.filter(_.userId === userId)
	
	import common._
	import productModel._
	
	private def getItems(cart: CartDto): Future[CartDto] =
		db run CartDetails.filter(c => c.cartId === cart.cartId).result flatMap (_.traverse { cd =>
			selectOne[ProductOptionItems, ProductOptionItemDto](ProductOptionItemDto.newInstance) { item =>
				item.productOptionItemId === cd.optionItemId.get
			} map (_.get)
		} map (items => cart.setList(items.toList)))
	
	def getCartsByUserId(implicit userId: String): Future[List[CartDto]] =
		select[Carts, CartDto](CartDto.newInstance) { cart => cart.userId === userId }
				.flatMap(_ traverse (cart => getItems(cart)))
	
	def getCartByCartId(implicit cartId: Int): Future[CartDto] =
		selectOne[Carts, CartDto](CartDto.newInstance) { cart => cart.cartId === cartId }.flatMap {
			case Some(cart) => getItems(cart)
			case None => Future.failed(throw new Exception())
		}
	
	private def insertCart(cart: CartDto)(f: CartDto => Future[Int]) = cart.itemList match {
		case Nil => Future.failed(new Exception())
		case h::t => {
			val surcharge = (h::t).map(_.surcharge).sum
			val query = Carts returning Carts.map(_.cartId)
			val row = CartsRow(cart.userId, 0,cart.name, cart.productId, cart.price, cart.quantity+surcharge, null)
			db run (query+=row) map(id => cart.setId(id)) flatMap(f(_))
		}
	}
	
	private def insertDetail(itemId: Int, cartId: Int) =
		db run (CartDetails += CartDetailsRow(0, Some(cartId), Some(itemId)))
		
	def addCart(cart: CartDto): Future[Int]=
		insertCart(cart) { cart =>
			cart.itemList.traverse { item =>
				insertDetail(item.productOptionItemId, cart.cartId)
			} map (_.sum)
		}
		
	def deleteCart(cq: Int)(implicit cartId: Int): Future[Int] =
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
		case _ => Future.failed(new Exception())
	}
	
}