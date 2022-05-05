package dto

import java.util.Date
import models.Tables.{ReviewImages, Reviews}
import play.api.libs.json.Json
import scala.language.implicitConversions

case class ReviewDto(productId: Int,
					 reviewId: Int,
					 userId: String,
					 rating: Int,
					 title: String,
					 content: String,
					 comment: String,
					 reviewDate: Date,
					 recommend: Int,
					 images: List[ReviewImageDto] = Nil) {
	
	def setImages(images: List[ReviewImageDto]): ReviewDto =
		ReviewDto(productId, reviewId, userId, rating, title, content, comment, reviewDate, recommend, images)
	
}

case class ReviewRequestDto(productId: Int,
							reviewId: Option[Int],
							userId: String,
							rating: Int,
							title: String,
							content: String,
							comment: Option[String],
							reviewDate: Option[Date],
							recommend: Option[Int],
							images: List[String] = Nil) {
	
}

case class ReviewImageDto(reviewId: Int,
						  reviewImageId: Int,
						  image: String,
						  sequence: Int)

object ReviewImageDto {
	implicit val imageWrite = Json.writes[ReviewImageDto]
	implicit val imageRead = Json.reads[ReviewImageDto]
	
	implicit def newInstance(i: ReviewImages#TableElementType): ReviewImageDto =
		ReviewImageDto(
			i.reviewId,
			i.reviewImageId,
			i.image,
			i.sequence
		)
}

object ReviewDto {
	implicit val reviewWrite = Json.writes[ReviewDto]
	implicit val reviewRead = Json.reads[ReviewDto]
	
	implicit def newInstance(r: Reviews#TableElementType): ReviewDto = {
		ReviewDto(r.productId, r.reviewId, r.userId, r.rating, r.title,
			r.content, r.comment, r.reviewDate, r.recommend)
	}
}
