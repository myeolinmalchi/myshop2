package dto

import models.Tables._
import play.api.libs.json.Json
import scala.language.implicitConversions
import slick.jdbc.GetResult

case class ProductDto(productId: Option[Int],
											name: String,
											sellerId: String,
											price: Int,
											categoryCode: String,
											detailInfo: String,
											thumbnail: String,
											reviewCount: Option[Int],
											rating: Option[Int],
											optionList: List[ProductOptionDto] = Nil,
											imageList: List[ProductImageDto] = Nil) {
	
	def setOptions(options: List[ProductOptionDto]): ProductDto =
		ProductDto(productId, name, sellerId, price, categoryCode, detailInfo, thumbnail, reviewCount, rating, options, imageList)
	
	def setImages(images: List[ProductImageDto]): ProductDto =
		ProductDto(productId, name, sellerId, price, categoryCode, detailInfo, thumbnail, reviewCount, rating, optionList, images)
	
	def setProductId(id: Int): ProductDto =
		ProductDto(Some(id), name, sellerId, price, categoryCode, detailInfo, thumbnail, reviewCount, rating, optionList, imageList)
	
}

case class ProductImageDto(productId: Option[Int],
													 productImageId: Option[Int],
													 image: String,
													 sequence: Int) {
	def setProductId(id: Int): ProductImageDto =
		ProductImageDto(Some(id), productImageId, image, sequence)
}


case class ProductOptionDto(productId: Option[Int],
														productOptionId: Option[Int],
														name: String,
														optionSequence: Int,
														itemList: List[ProductOptionItemDto] = Nil) {
	
	def setItems(items: List[ProductOptionItemDto]): ProductOptionDto =
		ProductOptionDto(productId, productOptionId, name, optionSequence, items)
	
	def setProductId(id: Int): ProductOptionDto =
		ProductOptionDto(Some(id), productOptionId, name, optionSequence, itemList)
		
	
	def setOptionId(id: Int): ProductOptionDto =
		ProductOptionDto(productId, Some(id), name, optionSequence, itemList)
}

case class ProductOptionItemDto(productOptionId: Option[Int],
																productOptionItemId: Option[Int],
																name: String,
																itemSequence: Int,
																surcharge: Int) {
	def setOptionId(id: Int): ProductOptionItemDto =
		ProductOptionItemDto(Some(id), productOptionItemId, name, itemSequence, surcharge)
	
	def setId(id: Int): ProductOptionItemDto =
		ProductOptionItemDto(productOptionId, Some(id), name, itemSequence, surcharge)
}

object ProductOptionItemDto {
	implicit val itemReads = Json.reads[ProductOptionItemDto]
	implicit val itemWrites = Json.writes[ProductOptionItemDto]
	implicit val newInstance = (i: ProductOptionItems#TableElementType) =>
		new ProductOptionItemDto(Some(i.productOptionId), Some(i.productOptionItemId), i.name, i.itemSequence, i.surcharge)
}

object ProductOptionDto {
	implicit val optionReads = Json.reads[ProductOptionDto]
	implicit val optionWrites = Json.writes[ProductOptionDto]
	implicit val newInstance: ProductOptions#TableElementType => ProductOptionDto =
		o => new ProductOptionDto(Some(o.productId), Some(o.productOptionId), o.name, o.optionSequence, Nil)
}

object ProductImageDto {
	implicit val imageRead = Json.reads[ProductImageDto]
	implicit val imageWrite = Json.writes[ProductImageDto]
	implicit val newInstance: ProductImages#TableElementType => ProductImageDto =
		img => new ProductImageDto(Some(img.productId), Some(img.productImageId), img.image, img.sequence)
}

object ProductDto {
	implicit val productReads = Json.reads[ProductDto]
	implicit val productWrites = Json.writes[ProductDto]
	implicit val newInstance: Products#TableElementType => ProductDto =
		p => new ProductDto(Some(p.productId), p.name, p.sellerId, p.price.toInt, p.categoryCode, p.detailInfo, p.thumbnail, Some(p
			.reviewCount), Some(p.rating), Nil, Nil)
	
	implicit val getProductResult = GetResult { r =>
		ProductDto(
			r.nextIntOption(),
			r.nextString(),
			r.nextString(),
			r.nextInt(),
			r.nextString(),
			r.nextString(),
			r.nextString(),
			r.nextIntOption(),
			r.nextIntOption()
		)
	}
	
}


