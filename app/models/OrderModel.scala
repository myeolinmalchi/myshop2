package models

import dto._
import javax.inject._
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

@Singleton
class OrderModel @Inject()(val dbConfigProvider: DatabaseConfigProvider,
													 productModel: ProductModel)
													(implicit ec: ExecutionContext)
	extends HasDatabaseConfigProvider[JdbcProfile] {
	
	object InnerApi {
		val orderQuery = (uid: String) => Orders.filter(_.userId === uid)
		val orderProductBySellerQuery = (sid: String) => OrderProducts.filter(_.sellerId === sid)
		
		def toDto[T, R](xs: Seq[T])(g: R => DBIOAction[R, NoStream, Effect.All])
									 (implicit f: T => R): DBIOAction[List[R], NoStream, Effect.All] =
			DBIO.sequence(xs.map(f andThen g).toList)
		
		type D[T] = DBIOAction[T, NoStream, Effect.All]
		
		def detailsToDto(details: Seq[OrderProductDetails#TableElementType]): D[List[ProductOptionItemDto]] =
			DBIO.sequence {
				details.toList map { detail =>
					for {
						itemOption <- ProductOptionItems
							.filter(_.productOptionItemId === detail.optionItemId)
							.result
							.headOption
						item = itemOption.getOrElse(throw new Exception())
					} yield ProductOptionItemDto.newInstance(item)
				}
			}
		
		def productsToDto(products: Seq[OrderProducts#TableElementType]): D[List[OrderProductDto]] =
			toDto(products) { p: OrderProductDto =>
				for {
					details <- OrderProductDetails
						.filter(_.orderProductId === p.orderProductId)
						.result
					itemDtoList <- detailsToDto(details)
					productOption <- Products
						.filter(_.productId === p.productId)
						.map(product => (product.name, product.thumbnail))
						.result
						.headOption
					(name, thumbnail) = productOption.getOrElse(throw new NoSuchElementException)
				} yield p.setList(itemDtoList).setName(name).setThumbnail(thumbnail)
			}
		
		def ordersToDto(orders: Seq[Orders#TableElementType]): D[List[OrderDto]] =
			toDto(orders) { o: OrderDto =>
				for {
					products <- OrderProducts
						.filter(_.orderId === o.orderId)
						.result
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
	
	def checkUserOrderedThisProduct(userId: String, productId: Int): Future[_] =
		db run (for {
			orders <- orderQuery(userId).result
			products <- DBIO.sequence(orders.map { order =>
				OrderProducts.filter(_.orderId === order.orderId).result
			}).map(_.flatten)
			productOption = products.find(_.productId == productId).map(_.orderProductId)
		} yield productOption.getOrElse(throw new Exception("구매내역이 없습니다.")))
	
	/** 사용자의 구매 내역에서 productId에 해당하는 상품 중 가장 최근 구매내역을 검색한다. */
	def getRecentlyOrderedProduct(userId: String, productId: Int): Future[Option[OrderProductDto]] = {
		db run (for {
			orders <- orderQuery(userId).result
			products <- DBIO.sequence(orders.map { order =>
				val result: DBIOAction[Seq[OrderProducts#TableElementType], NoStream, Effect.All] =
					OrderProducts.filter(_.orderId === order.orderId).result
				result
			}).map(_.flatten)
			productOption = products.find(_.productId == productId)
//			result <- DBIO.sequenceOption(productOption
//				.map(OrderProductDto.newInstance)
//				.map { orderProduct =>
//					val result: DBIOAction[OrderProductDto, NoStream, Effect.Read] = for {
//						details <- OrderProductDetails
//							.filter(_.orderProductId === orderProduct.orderProductId)
//							.result
//						optionItems <- DBIO.sequence(details.map { detail =>
//							val result: DBIOAction[ProductOptionItemDto, NoStream, Effect.Read] = for {
//								item <- ProductOptionItems.filter(_.productOptionItemId === detail.optionItemId).result.headOption
//							} yield item.map(ProductOptionItemDto.newInstance).getOrElse(throw new NoSuchElementException)
//							result
//						})
//					} yield orderProduct.setList(optionItems.toList)
//					result
//				})
		} yield productOption.map(OrderProductDto.newInstance))
	}
	
	
	def getOrderProductsBySellerId(sellerId: String): Future[List[OrderProductDto]] =
		db run (for {
			products <- orderProductBySellerQuery(sellerId).result
			productDtoList <- productsToDto(products)
		} yield productDtoList)
	
}
