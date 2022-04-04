package models

import cats.implicits.toTraverseOps
import dto._
import java.sql.Timestamp
import javax.inject._
import models.Tables._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.MySQLProfile.api._

class OrderModel(db: Database)(implicit ec: ExecutionContext) {

	object InnerApi {
		val orderQuery = (uid: String) => Orders.filter(_.userId === uid)
		val orderProductBySellerQuery = (sid: String) => OrderProducts.filter(_.sellerId === sid)
		val orderProductQuery = (oid: Int) => OrderProducts.filter(_.orderId === oid)
		val detailQuery = (pid: Int) => OrderProductDetails.filter(_.orderProductId === pid)
		val itemQuery = (iid: Int) => ProductOptionItems.filter(_.productOptionItemId === iid)
		
		def toDto[T, R](xs: Seq[T])(g: R => DBIOAction[R, NoStream, Effect.All])
					   (implicit f: T => R): DBIOAction[List[R], NoStream, Effect.All] =
			DBIO.sequence(xs.map(f andThen g).toList)
			
		type D[T] = DBIOAction[T, NoStream, Effect.All]
		
		def detailsToDto(details: Seq[OrderProductDetails#TableElementType]): D[List[ProductOptionItemDto]] =
			DBIO.sequence {
				details.toList map { detail =>
					for {
						itemOption <- itemQuery(detail.optionItemId).result.headOption
						item = itemOption.getOrElse(throw new Exception())
					} yield ProductOptionItemDto.newInstance(item)
				}
			}
		
		def productsToDto(products: Seq[OrderProducts#TableElementType]): D[List[OrderProductDto]] =
			toDto(products) { p: OrderProductDto =>
				for {
					details <- detailQuery(p.orderProductId).result
					itemDtoList <- detailsToDto(details)
				} yield p.setList(itemDtoList)
			}
		
		def ordersToDto(orders: Seq[Orders#TableElementType]): D[List[OrderDto]] =
			toDto(orders) { o: OrderDto =>
				for {
					products <- orderProductQuery(o.orderId).result
					productDtoList <- productsToDto(products)
				} yield o.setList(productDtoList)
			}
	}
	
	import InnerApi._

	def getOrdersByUserId(userId: String): Future[List[OrderDto]] =
		db run (for {
			orders <- orderQuery(userId).result
			orderDtoList <- ordersToDto(orders)
		} yield orderDtoList)
		
	def getOrderProductsBySellerId(sellerId: String): Future[List[OrderProductDto]] =
		db run (for {
			products <- orderProductBySellerQuery(sellerId).result
			productDtoList <- productsToDto(products)
		} yield productDtoList)
	
}