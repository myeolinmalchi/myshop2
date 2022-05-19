package services.product

import common.validation.ValidationResultLib
import dto.{ReviewRequestDto, ReviewResponseDto}
import javax.inject.{Inject, Singleton}
import models.ReviewModel
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CommunicateServiceImpl @Inject() (reviewModel: ReviewModel)
									   (implicit ec: ExecutionContext)
		extends CommunicateService with ValidationResultLib[Future] {
	
	override def getReviewsByProductId(productId: Int): Future[List[ReviewResponseDto]] =
		reviewModel getReviewsByProductId productId
	
	override def getReviewsByUserId(userId: String): Future[List[ReviewResponseDto]] =
		reviewModel getReviewsByUserId userId
	
	override def getReviewsByProductIdWithPagination(productId: Int, size: Int, page: Int): Future[List[ReviewResponseDto]] =
		reviewModel getReviewsByProductIdWithPagination (productId, size, page)
	
	override def getReviewsByUserIdWithPagination(userId: String, size: Int, page: Int): Future[List[ReviewResponseDto]] =
		reviewModel getReviewsByUserIdWithPagination (userId, size, page)
	
	override def getReviewCountsByProductId(productId: Int): Future[Int] =
		reviewModel getReviewCountByProductId productId
	
	override def getReviewCountsByUserId(userId: String): Future[Int] =
		reviewModel getReviewCountByUserId userId
	
	/*
	* TODO:
	*  리뷰 작성 validation
	*  제목 ~~자 이하
	*  내용 ~~자 이하
	* */
	override def addReview(review: ReviewRequestDto): Future[Int] = ???

}
