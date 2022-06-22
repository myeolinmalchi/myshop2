package models

import dto.{ReviewDto, ReviewImageDto, ReviewRequestDto, ReviewResponseDto}
import javax.inject.{Inject, Singleton}
import models.Tables.{OrderProductDetails, ProductOptionItems, Products, ReviewImages, ReviewImagesRow, Reviews}
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
		
	private def getReviews(rs: DBIOAction[Seq[Reviews#TableElementType], NoStream, Effect.Read]): Future[List[ReviewResponseDto]] = db run {
		for {
			reviews <- rs
			result <- toDto(reviews) { r: ReviewResponseDto =>
				for {
					images <- ReviewImages
						.filter(_.reviewId === r.reviewId)
						.result
					imageDtoList = images.map(ReviewImageDto.newInstance).toList
					details <- OrderProductDetails
						.filter(_.orderProductId === r.orderProductId)
						.result
					optionItemOptions <- DBIO.sequence {
						details.map { detail =>
							ProductOptionItems
								.filter(_.productOptionItemId === detail.optionItemId)
								.result
								.headOption
						}.toList
					}
					optionItemName = optionItemOptions
						.flatten
						.map(_.name)
						.reduce(_ + ", " + _)
					productNameOption <- Products
						.filter(_.productId === r.productId)
						.map(_.name)
						.result
						.headOption
					productName = productNameOption
						.getOrElse(throw new NoSuchElementException)
				} yield r
					.setName(s"$productName($optionItemName)")
					.setImages(imageDtoList)
			}
		} yield result
	}
	
	def getReviewsByProductId(productId: Int): Future[List[ReviewResponseDto]] =
		getReviews(Reviews.filter(_.productId === productId).result)
		
	def getReviewsByUserId(userId: String): Future[List[ReviewResponseDto]] =
		getReviews(Reviews.filter(_.userId === userId).result)
		
	def getReviewsByProductIdWithPagination(productId: Int,
																					size: Int,
																					page: Int): Future[List[ReviewResponseDto]] =
		getReviews (
			Reviews
				.filter(_.productId === productId)
				.sorted(_.reviewId.desc)
				.drop((page-1)*size)
				.take(size)
				.result
		)
		
	def getReviewsByUserIdWithPagination(userId: String,
																			 size: Int,
																			 page: Int): Future[List[ReviewResponseDto]] =
		getReviews (
			Reviews
				.filter(_.userId === userId)
				.sorted(_.reviewId.desc)
				.drop((page-1)*size)
				.take(size)
				.result
		)
		
	def getReviewCountByProductId(productId: Int): Future[Int] =
		db run Reviews.filter(_.productId === productId).size.result
		
	def getReviewCountByUserId(userId: String): Future[Int] =
		db run Reviews.filter(_.userId === userId).size.result
	
		
	def insertReview(review: ReviewRequestDto): Future[Int] = {
		val reviewQuery = Reviews.map(r => (r.productId, r.userId, r.rating, r.title, r.content, r.orderProductId)) returning Reviews.map(_.reviewId)
		val reviewRow = (review.productId, review.userId, review.rating, review.title, review.content, review.orderProductId)
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
