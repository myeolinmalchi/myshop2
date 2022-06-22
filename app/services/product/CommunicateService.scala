package services.product

import com.google.inject.ImplementedBy
import dto.{OrderProductDto, QnaRequestDto, QnaResponseDto, ReviewDto, ReviewRequestDto, ReviewResponseDto}
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
	def getRecentlyOrderedProduct(userId: String, productId: Int): Future[Option[OrderProductDto]]
	
	def getQnasByUserIdWithPagination(userId: String, size: Int, page: Int): Future[List[QnaResponseDto]]
	def getQnaCountsByUserId(userId: String): Future[Int]
	def getQnasByProductIdWithPagination(productId: Int, size: Int, page: Int): Future[List[QnaResponseDto]]
	def getQnaCountsByProductId(productId: Int): Future[Int]
	def addQna(qna: QnaRequestDto): Future[Int]
	
}
