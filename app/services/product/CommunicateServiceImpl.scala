package services.product

import common.validation.ValidationResultLib
import dto.{ReviewDto, ReviewRequestDto}
import javax.inject.{Inject, Singleton}
import models.ReviewModel
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CommunicateServiceImpl @Inject() (reviewModel: ReviewModel)
									   (implicit ec: ExecutionContext)
		extends CommunicateService with ValidationResultLib[Future] {
	
	override def getReviewsByProductId(productId: Int): Future[List[ReviewDto]] =
		reviewModel getReviewsByProductId productId
	
	/*
	* TODO:
	*  리뷰 작성 validation
	*  제목 ~~자 이하
	*  내용 ~~자 이하
	* */
	override def addReview(review: ReviewRequestDto): Future[Int] = ???

}
