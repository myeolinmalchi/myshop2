package services.product

import com.google.inject.ImplementedBy
import dto.ReviewDto
import scala.concurrent.Future

trait CommunicateService {
	
	def getReviewsByProductId(productId: Int): Future[List[ReviewDto]]
	
}
