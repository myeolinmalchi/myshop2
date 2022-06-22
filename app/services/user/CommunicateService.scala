package services.user

import dto.ReviewRequestDto
import scala.concurrent.Future

trait CommunicateService {
	def writeReview(review: ReviewRequestDto): Future[Int]
	def deleteReview(reviewId: Int): Future[_]
	def recommendReview(reviewId: Int): Future[_]
}
