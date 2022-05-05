package models

import dto.{ReviewDto, ReviewImageDto, ReviewRequestDto}
import javax.inject.{Inject, Singleton}
import models.Tables.{Products, ReviewImages, ReviewImagesRow, Reviews}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

@Singleton
class ReviewModel @Inject() (val dbConfigProvider: DatabaseConfigProvider)
							(implicit ec: ExecutionContext)
		extends HasDatabaseConfigProvider[JdbcProfile] {
	
	def toDto[T, R](xs: Seq[T])(g: R => DBIOAction[R, NoStream, Effect.All])
				   (implicit f: T => R): DBIOAction[List[R], NoStream, Effect.All] =
		DBIO.sequence(xs.map(f andThen g).toList)
		
	private def getReviews(f: Reviews => Rep[Boolean]): Future[List[ReviewDto]] =
		db run (for {
			reviews <- Reviews.filter(f).result
			result <- toDto(reviews) { r: ReviewDto =>
				for {
					images <- ReviewImages.filter(_.reviewId === r.reviewId).result
					imageDtoList = images.map(ReviewImageDto.newInstance).toList
				} yield r.setImages(imageDtoList)
			}
		} yield result)
	
	def getReviewsByProductId(productId: Int): Future[List[ReviewDto]] =
		getReviews(_.productId === productId)
		
	def getReviewsByUserId(userId: String): Future[List[ReviewDto]] =
		getReviews(_.userId === userId)
		
	def insertReview(review: ReviewRequestDto): Future[Int] = {
		val reviewQuery = Reviews.map(r => (r.productId, r.userId, r.rating, r.title, r.content)) returning Reviews.map(_.reviewId)
		val reviewRow = (review.productId, review.userId, review.rating, review.title, review.content)
		val productReviewQuery = Products
				.filter(_.productId === review.productId)
				.map(product => (product.rating, product.reviewCount))
		db run {
			for {
				reviewId <- reviewQuery += reviewRow
				aff <- ReviewImages ++= review.images.zipWithIndex.map { case (image, index) =>
					ReviewImagesRow(reviewId, 0, image, index+1)
				}
				productOption <- productReviewQuery.result.headOption
				updateOption = productOption map { p =>
					productReviewQuery.update((p._1 + review.rating, p._2 + 1))
				}
				aff2 <- updateOption.getOrElse(DBIO.successful(0))
			} yield aff.sum + aff2
		}.transactionally
	}
}
