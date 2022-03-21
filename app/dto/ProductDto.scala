package dto

import models.Tables.{ProductOptionItems, ProductOptions, Products}
import play.api.libs.json.Json

object readAndWrites {
	implicit val itemRead = Json.reads[ProductOptionItemDto]
	implicit val itemWrite = Json.writes[ProductOptionItemDto]
	
	implicit val optionRead = Json.reads[ProductOptionDto]
	implicit val optionWrite = Json.writes[ProductOptionDto]
	
	implicit lazy val productRead = Json.reads[ProductDto]
	implicit lazy val productWrite = Json.writes[ProductDto]
}

case class ProductDto(productId: Int,
					  name: String,
					  sellerId: String,
					  price: Int,
					  categoryCode: String,
					  detailInfo: String,
					  thumbnail: String,
					  reviewCount: Int,
					  rating: Int,
					  var optionList: List[ProductOptionDto] = Nil) {
	
	def setOptions(options: List[ProductOptionDto]): ProductDto = {
		this.optionList = options
		this
	}
}

case class ProductOptionDto(productId: Int,
							productOptionId: Int,
							name: String,
							optionSequence: Int,
							images: String,
							var itemList: List[ProductOptionItemDto] = Nil){
	
	def setItems(items: List[ProductOptionItemDto]): ProductOptionDto = {
		this.itemList = items
		this
	}
}

case class ProductOptionItemDto(productOptionId: Int,
							   productOptionItemId: Int,
							   name: String,
							   itemSequence: Int,
							   surcharge: Int,
							   stock: Int)

object ProductDto {
	def newInstance(p: Products#TableElementType) =
		new ProductDto(p.productId, p.name, p.sellerId, p.price.toInt, p.categoryCode, p.detailInfo, p.thumbnail, p
				.reviewCount, p.rating, Nil)
}

object ProductOptionDto {
	def newInstance(o: ProductOptions#TableElementType) =
		new ProductOptionDto(o.productId, o.productOptionId, o.name, o.optionSequence, o.images, Nil)
	
}

object ProductOptionItemDto {
	def newInstance(i: ProductOptionItems#TableElementType) =
		new ProductOptionItemDto(i.productOptionId, i.productOptionItemId, i.name, i.itemSequence, i.surcharge, i
				.stock)
}
