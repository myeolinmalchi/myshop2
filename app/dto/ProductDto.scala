package dto

import models.Tables._
import play.api.libs.json.Json
import scala.language.implicitConversions
import slick.jdbc.GetResult

case class ProductDto(var productId: Int,
					  name: String,
					  sellerId: String,
					  price: Int,
					  categoryCode: String,
					  detailInfo: String,
					  thumbnail: String,
					  reviewCount: Int,
					  rating: Option[Int],
					  var optionList: List[ProductOptionDto] = Nil,
					  var imageList: List[ProductImageDto] = Nil) {
	
	def setOptions(options: List[ProductOptionDto]): ProductDto = {
		this.optionList = options
		this
	}
	
	def setImages(images: List[ProductImageDto]): ProductDto = {
		this.imageList = images
		this
	}
	
	def setProductId(id: Int): ProductDto = {
		this.productId = id
		this
	}
}

case class ProductImageDto(var productId: Int,
						   productImageId: Int,
						   image: String,
						   sequence: Int) {
	def setProductId(id: Int): ProductImageDto = {
		this.productId = id
		this
	}
}


case class ProductOptionDto(var productId: Int,
							var productOptionId: Int,
							name: String,
							optionSequence: Int,
							var itemList: List[ProductOptionItemDto] = Nil){
	
	def setItems(items: List[ProductOptionItemDto]): ProductOptionDto = {
		this.itemList = items
		this
	}
	
	def setProductId(id: Int): ProductOptionDto = {
		this.productId = id
		this
	}
	
	def setOptionId(id: Int): ProductOptionDto = {
		this.productOptionId = id
		this
	}
}

case class ProductOptionItemDto(var productOptionId: Int,
								var productOptionItemId: Int,
							   name: String,
							   itemSequence: Int,
							   surcharge: Int){
	def setOptionId(id: Int): ProductOptionItemDto = {
		this.productOptionId = id
		this
	}
	
	def setId(id: Int): ProductOptionItemDto = {
		this.productOptionItemId = id
		this
	}
}

object ProductOptionItemDto {
	implicit val itemReads = Json.reads[ProductOptionItemDto]
	implicit val itemWrites = Json.writes[ProductOptionItemDto]
	implicit val newInstance = (i: ProductOptionItems#TableElementType) =>
		new ProductOptionItemDto(i.productOptionId, i.productOptionItemId, i.name, i.itemSequence, i.surcharge)
}
object ProductOptionDto{
	implicit val optionReads = Json.reads[ProductOptionDto]
	implicit val optionWrites = Json.writes[ProductOptionDto]
	implicit val newInstance: ProductOptions#TableElementType => ProductOptionDto =
		o => new ProductOptionDto(o.productId, o.productOptionId, o.name, o.optionSequence, Nil)
}
object ProductImageDto {
	implicit val imageRead = Json.reads[ProductImageDto]
	implicit val imageWrite = Json.writes[ProductImageDto]
	implicit val newInstance: ProductImages#TableElementType => ProductImageDto =
		img => new ProductImageDto(img.productId, img.productImageId, img.image, img.sequence)
}
object ProductDto {
	implicit val productReads = Json.reads[ProductDto]
	implicit val productWrites = Json.writes[ProductDto]
	implicit val newInstance: Products#TableElementType => ProductDto =
		p => new ProductDto(p.productId, p.name, p.sellerId, p.price.toInt, p.categoryCode, p.detailInfo, p.thumbnail, p
				.reviewCount, Some(p.rating), Nil, Nil)
				
	implicit val getProductResult = GetResult { r =>
		ProductDto(
			r.nextInt(),
			r.nextString(),
			r.nextString(),
			r.nextInt(),
			r.nextString(),
			r.nextString(),
			r.nextString(),
			r.nextInt(),
			r.nextIntOption()
		)
	}
	
}


