package services.user

import dto.UserDto
import play.api.mvc.{AnyContent, Request, Result}
import scala.concurrent.{ExecutionContext, Future}

trait AuthService {
	
	def withUserAuth(id: String)(f: UserDto => Future[Result])
					(implicit request: Request[AnyContent], ec: ExecutionContext,
					 accountService: AccountService): Future[Result]
	
}
