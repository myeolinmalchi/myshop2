package services.product

import com.google.inject.ImplementedBy
import dto.{ReviewDto, ReviewRequestDto, ReviewResponseDto}
import scala.concurrent.Future

@ImplementedBy(classOf[CommunicateServiceImpl])
trait CommunicateService {
	
	def getReviewsByProductId(productId: Int): Future[List[ReviewResponseDto]]
	def getReviewsByUserId(userId: String): Future[List[ReviewResponseDto]]
	def getReviewsByProductIdWithPagination(productId: Int, size: Int, page: Int): Future[List[ReviewResponseDto]]
	def getReviewsByUserIdWithPagination(userId: String, size: Int, page: Int): Future[List[ReviewResponseDto]]
	def getReviewCountsByProductId(productId: Int): Future[Int]
	def getReviewCountsByUserId(userId: String): Future[Int]
	def addReview(review: ReviewRequestDto): Future[Int]
	
}
