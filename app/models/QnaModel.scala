package models

import dto.{QnaRequestDto, QnaResponseDto}
import javax.inject.{Inject, Singleton}
import models.Tables.Qnas
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

@Singleton
class QnaModel @Inject()(val dbConfigProvider: DatabaseConfigProvider)
												(implicit ec: ExecutionContext)
	extends HasDatabaseConfigProvider[JdbcProfile] {
	
	def getQnaWithPagination(filter: Qnas => Rep[Boolean],
													 size: Int,
													 page: Int): Future[List[QnaResponseDto]] = db run {
		for {
			qnas <- Qnas.filter(filter)
				.sorted(_.qnaId.desc)
				.drop((page - 1) * size)
				.take(size)
				.result
		} yield qnas
			.map(QnaResponseDto.newInstance)
			.toList
	}
	
	def getQnaByUserIdWithPagination(userId: String,
																	 size: Int,
																	 page: Int): Future[List[QnaResponseDto]] =
		getQnaWithPagination (_.userId === userId, size, page)
		
	def getQnaCountByUserId(userId: String): Future[Int] =
		db run Qnas.filter(_.userId === userId).size.result
	
	def getQnaCountByProductId(productId: Int): Future[Int] =
		db run Qnas.filter(_.productId === productId).size.result
	
	def getQnaByProductIdWithPagination(productId: Int,
																			size: Int,
																			page: Int): Future[List[QnaResponseDto]] =
		getQnaWithPagination(_.productId === productId, size, page)
		
	def insertQna(qna: QnaRequestDto): Future[Int] = db run {
		Qnas.map(qna => (qna.productId, qna.userId, qna.question)) += (qna.productId, qna.userId, qna.question)
	}
	
}
