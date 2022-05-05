package services.product

import com.google.inject.ImplementedBy
import dto.{ReviewDto, ReviewRequestDto}
import scala.concurrent.Future

@ImplementedBy(classOf[CommunicateServiceImpl])
trait CommunicateService {
	
	def getReviewsByProductId(productId: Int): Future[List[ReviewDto]]
	def addReview(review: ReviewRequestDto): Future[Int]
	
}
