package controllers

import play.api.libs.json.{Json, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object Common {
	
	implicit class CustomFuture[T](a: Future[T])(implicit ec: ExecutionContext) {
		private def orError(f: T => Result): Future[Result] =
			a map f recover {
				case ex: Exception => ex toJsonError
			}
		
		def trueOrError: Future[Result] = orError { _ =>
			Ok(Json.toJson(true))
		}
		
		def getOrError(implicit w: Writes[T]): Future[Result] = orError { result =>
			Ok(Json.toJson(result))
		}
	}
	
	implicit class CustomException(e: Exception) {
		def toJsonError: Result = Ok(Json.toJson(Map("error" -> e.getMessage)))
	}
}
