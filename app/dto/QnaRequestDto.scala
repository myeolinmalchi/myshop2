package dto

import java.util.Date
import models.Tables.Qnas
import play.api.libs.json.Json

case class QnaRequestDto(productId: Int,
												 userId: String,
												 question: String)

case class QnaResponseDto(productId: Int,
													qnaId: Int,
													userId: String,
													question: String,
													answer: Option[String],
													questionDate: Date,
													answerDate: Option[Date]) {
	
}

object QnaResponseDto {
	implicit def newInstance(row: Qnas#TableElementType): QnaResponseDto = {
		QnaResponseDto(
			row.productId,
			row.qnaId,
			row.userId,
			row.question,
			row.answer,
			row.questionDate,
			row.answerDate
		)
	}
	implicit val qnaResponseWrite = Json.writes[QnaResponseDto]
}

object QnaRequestDto {
	implicit val qnaRequestDto = Json.reads[QnaRequestDto]
}