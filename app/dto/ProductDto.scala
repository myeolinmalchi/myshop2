package dto

import models.Tables._
import play.api.libs.json.Json

object readAndWrites {
	implicit val itemRead = Json.reads[ProductOptionItemDto]
	implicit val itemWrite = Json.writes[ProductOptionItemDto]
	
	implicit val optionRead = Json.reads[ProductOptionDto]
	implicit val optionWrite = Json.writes[ProductOptionDto]
	
	implicit val imageRead = Json.reads[ProductImageDto]
	implicit val imageWrite = Json.writes[ProductImageDto]
	
	implicit lazy val productRead = Json.reads[ProductDto]
	implicit lazy val productWrite = Json.writes[ProductDto]
	
	implicit val cartRead = Json.reads[CartDto]
	implicit val cartWrite = Json.writes[CartDto]
}

case class ProductDto(var productId: Int,
					  name: String,
					  sellerId: String,
					  price: Int,
					  categoryCode: String,
					  detailInfo: String,
					  thumbnail: String,
					  reviewCount: Int,
					  rating: Int,
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

object ProductImageDto {
	def newInstance(img: ProductImages#TableElementType): ProductImageDto =
		new ProductImageDto(img.productId, img.productImageId, img.image, img.sequence)
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
							   productOptionItemId: Int,
							   name: String,
							   itemSequence: Int,
							   surcharge: Int){
	def setOptionId(id: Int): ProductOptionItemDto = {
		this.productOptionId = id
		this
	}
}

object ProductDto {
	def newInstance(p: Products#TableElementType) =
		new ProductDto(p.productId, p.name, p.sellerId, p.price.toInt, p.categoryCode, p.detailInfo, p.thumbnail, p
				.reviewCount, p.rating, Nil, Nil)
}

object ProductOptionDto {
	def newInstance(o: ProductOptions#TableElementType) =
		new ProductOptionDto(o.productId, o.productOptionId, o.name, o.optionSequence, Nil)
	
}

object ProductOptionItemDto {
	def newInstance(i: ProductOptionItems#TableElementType) =
		new ProductOptionItemDto(i.productOptionId, i.productOptionItemId, i.name, i.itemSequence, i.surcharge)
}
