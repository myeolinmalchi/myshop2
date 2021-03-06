package dto

import java.util.Date
import models.Tables.{OrderProducts, Orders}
import play.api.libs.json.Json
import scala.language.implicitConversions

case class OrderDto(orderId: Int,
										userId: String,
										orderDate: Date,
										var products: List[OrderProductDto]) {
	def setList(products: List[OrderProductDto]) = {
		this.products = products
		this
	}
}

case class OrderProductDto(orderId: Int,
													 orderProductId: Int,
													 productId: Int,
													 sellerId: String,
													 quantity: Int,
													 address: String,
													 state: Int,
													 name: Option[String],
													 thumbnail: Option[String],
													 var details: List[ProductOptionItemDto]) {
	def setList(details: List[ProductOptionItemDto]) = {
		this.details = details
		this
	}
	
	def setName(name: String): OrderProductDto =
		OrderProductDto(orderId, orderProductId, productId, sellerId, quantity, address, state, Some(name), thumbnail, details)
		
	def setThumbnail(thumbnail: String): OrderProductDto =
		OrderProductDto(orderId, orderProductId, productId, sellerId, quantity, address, state, name, Some(thumbnail), details)
	
}

object OrderProductDto {
	implicit val itemReads = Json.reads[ProductOptionItemDto]
	implicit val itemWrites = Json.writes[ProductOptionItemDto]
	
	implicit val productReads = Json.reads[OrderProductDto]
	implicit val productWrites = Json.writes[OrderProductDto]
	
	implicit def newInstance(product: OrderProducts#TableElementType) =
		new OrderProductDto(product.orderId, product.orderProductId, product.productId, product.sellerId, product
			.quantity, product.address, product.state, None, None, Nil)
}

object OrderDto {
	implicit val orderReads = Json.reads[OrderDto]
	implicit val orderWirtes = Json.writes[OrderDto]
	
	implicit def newInstance(order: Orders#TableElementType) =
		new OrderDto(order.orderId, order.userId, order.orderDate, Nil)
}