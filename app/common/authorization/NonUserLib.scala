package common.authorization

import models.NonUserModel
import play.api.mvc.{AnyContent, Cookie, Request, Result}
import scala.concurrent.{ExecutionContext, Future}

trait NonUserLib {
	def withNonUserToken(f: String => Future[Result])
											(implicit request: Request[AnyContent], ec: ExecutionContext): Future[Result] =
		request.cookies.get("idToken") match {
			case Some(idToken) => f(idToken.value)
			case None =>
				val token = NonUserModel.generateToken
				f(token) map(_.withCookies(
					new Cookie("idToken", token, Some(604800), httpOnly = false)))
		}
}
