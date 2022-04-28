package common.json

import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Reads}
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{AnyContent, Call, Request, Result}
import scala.concurrent.{ExecutionContext, Future}

object CustomJsonApi {
	
	def withJsonDto[A](f: A => Future[Result])
					  (implicit request: Request[AnyContent],
					   reads: Reads[A], defaultPage: Call): Future[Result] = {
		request.body.asJson.map { body =>
			Json.fromJson[A](body) match {
				case JsSuccess(a, path) => f(a)
				case e @JsError(_) =>
//					logger.info(s"Invalid request body detected: ${e.toString}")
					Future.successful(Redirect(defaultPage))
			}
		}.getOrElse(Future.successful(Redirect(defaultPage)))
	}
	
	def withAnyJson(f: JsValue => Future[Result])
				   (implicit request: Request[AnyContent], ec: ExecutionContext): Future[Result] = {
		request.body.asJson match {
			case Some(value) => f(value)
			case None =>
//				logger.info(s"No request body detected")
				Future(Ok(Json.toJson(Map("error" -> "올바르지 않은 요청입니다."))))
		}
	}
}