package dto

import java.util.Date
import models.Tables.Reviews
import play.api.libs.json.Json

case class ReviewDto(productId: Int,
					 reviewId: Int,
					 userId: String,
					 rating: Int,
					 title: String,
					 content: String,
					 comment: String,
					 reviewDate: Date,
					 recommend: Int) {
	
}

object ReviewDto {
	implicit val reviewWrite = Json.writes[ReviewDto]
	implicit val reviewRead = Json.reads[ReviewDto]
	
	implicit def newInstance(r: Reviews#TableElementType): ReviewDto = {
		new ReviewDto(r.productId, r.reviewId, r.userId, r.rating, r.title,
			r.content, r.comment, r.reviewDate, r.recommend)
	}
}
