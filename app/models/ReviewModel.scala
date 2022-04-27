package models

import dto.ReviewDto
import javax.inject.{Inject, Singleton}
import models.Tables.{Products, Reviews}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

@Singleton
class ReviewModel @Inject() (val dbConfigProvider: DatabaseConfigProvider)
							(implicit ec: ExecutionContext)
		extends HasDatabaseConfigProvider[JdbcProfile] {
	
	def getReviewsByProductId(productId: Int): Future[List[ReviewDto]] =
		db run (for {
			reviews <- Reviews.filter(_.productId === productId).result
		} yield reviews.map(ReviewDto.newInstance).toList)
		
	def getReviewsByUserId(userId: String): Future[List[ReviewDto]] =
		db run (for {
			reviews <- Reviews.filter(_.userId === userId).result
		} yield reviews.map(ReviewDto.newInstance).toList)
		
	def insertReview(review: ReviewDto): Future[Int] = {
		val insertQuery = (Reviews.map(r => (r.productId, r.userId, r.rating, r.title, r.content ))
			+= (review.productId, review.userId, review.rating, review.title, review.content))
		val productReviewQuery = Products.filter(_.productId === review.productId)
				.map(product => (product.rating, product.reviewCount))
		db run (for {
			aff1 <- insertQuery
			productReviewOption <- productReviewQuery.result.headOption
			updateOption = productReviewOption.map{ r =>
				productReviewQuery.update((r._1+review.rating, r._2+1))
			}
			aff2 <- updateOption.getOrElse(DBIO.successful(0))
		} yield aff1 + aff2).transactionally
	}
}
