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
class NonUserCartModelImpl @Inject()(val dbConfigProvider: DatabaseConfigProvider,
																		 productModel: ProductModel)
																		(implicit ec: ExecutionContext)
	extends HasDatabaseConfigProvider[JdbcProfile] with NonUserCartModel{
	
	type D[T] = DBIOAction[T, NoStream, Effect.All]
	
	override def getCartsByToken(token: String): Future[List[CartDto]] =
		db run sql"SELECT * FROM v_non_user_carts WHERE id_token = $token".as[CartDto] map(_.toList)
	
	override def getCartByCartId(cartId: Int): Future[Option[CartDto]] =
		db run sql"SELECT * FROM v_non_user_carts WHERE non_user_cart_id = $cartId".as[CartDto].headOption
	
	override def getItemIdsByCartId(cartId: Int): Future[List[Int]] = db run {
		for {
			ids <- NonUserCartDetails
				.filter(_.nonUserCartId === cartId)
				.map(_.optionItemId)
				.result
		} yield ids.toList.map(_.toInt)
	}
	
	override def insertCart(cart: CartRequestDto): Future[Int] = {
		val query = NonUserCarts.map(c => (c.idToken, c.productId, c.quantity)) returning NonUserCarts.map(_.nonUserCartId)
		val row = (cart.userId, cart.productId, cart.quantity)
		
		db run {
			for {
				cartId <- query += row
				aff <- DBIO.sequence (
					cart.itemList map { itemId =>
						NonUserCartDetails.map(d => (d.nonUserCartId, d.optionItemId)) += (cartId, itemId)
					}
				)
			} yield aff.sum
		}.transactionally
	}
	
	override def deleteCart(cartId: Int): Future[Int] =
		db run NonUserCarts.filter(_.nonUserCartId === cartId).delete
	
	override def updateQuantity(quantity: Int, cartId: Int): Future[Int] = db run {
		if (quantity > 0)
			NonUserCarts
				.filter(_.nonUserCartId === cartId)
				.map(_.quantity)
				.update(quantity)
		else DBIO.successful(0)
	}
	
}

