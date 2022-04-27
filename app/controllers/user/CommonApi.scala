package controllers.user

import dto.UserDto
import java.time.LocalDateTime
import models.{NonUserModel, UserSessionModel}
import play.api.mvc.Results.Redirect
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import services._

object CommonApi {
	
	implicit val defaultPage: Call = controllers.user.routes.IndexController.index
	
	case class WithUser(block: UserDto => Future[Result])
					   (implicit request: Request[AnyContent],
						ec: ExecutionContext, service: user.AccountService){
		
		def extractUser(req: RequestHeader): Future[Option[UserDto]] = {
			val sessionTokenOpt = req.session.get("sessionToken")
			def swap[M](x: Option[Future[M]]): Future[Option[M]] =
				Future.sequence(Option.option2Iterable(x)).map(_.headOption)
			swap (sessionTokenOpt
					.flatMap(token => UserSessionModel.getSession(token))
					.filter(_.expiration.isAfter(LocalDateTime.now()))
					.map(_.userId)
					.map(service.getUser))
		}
		
		def ifNot(result: => Future[Result]): Future[Result] =
			extractUser(request) flatMap {
				case Some(user) => block(user)
				case None => result.map(_.withSession(request.session - "sessionToken"))
			}
		
		def onlyForWithout(result: => Future[Result]): Future[Result] =
			extractUser(request) flatMap {
				case Some(user) => block(user)
				case None => result
			}
		
		def endWith: Future[Result] =
			extractUser(request) flatMap {
				case Some(user) => block(user)
				case None => Future(Redirect(controllers.user.routes.AccountController.loginPage)
						.flashing("error" -> "로그인이 필요합니다.")
						.withSession(request.session - "sessionToken"))
			}
	}
	
	// 쿠키에서 비회원 토큰을 조회(없을경우 생성)하여 동작을 수행한다.
	// withUser 메서드와 함께 사용한다.
	// 쿠키는 일주일(604800초)동안 유지된다.
	def withNonUserToken(f: String => Future[Result])
						(implicit request: Request[AnyContent], ec: ExecutionContext): Future[Result] =
		request.cookies.get("idToken") match {
			case Some(idToken) => f(idToken.value)
			case None =>
				val token = NonUserModel.generateToken
				f(token) map(_.withCookies(
					new Cookie("idToken", token, Some(604800), httpOnly = false)))
		}
	
	def withUser(block: UserDto => Future[Result])
				(implicit request: Request[AnyContent],
				 ec: ExecutionContext, service: user.AccountService): WithUser = WithUser(block)
	
	def withoutUser(block: Unit => Future[Result])
				   (implicit request: Request[AnyContent],
					ec: ExecutionContext, service: user.AccountService): Future[Result] =
		WithUser {
			_ => Future(Redirect(defaultPage)
					.flashing("error" -> "이미 로그인 중입니다."))
		} onlyForWithout block()
	
	
	def withoutUser(block: Future[Result])
				   (implicit request: Request[AnyContent],
					ec: ExecutionContext, service: user.AccountService): Future[Result] =
		WithUser {
			_ => Future(Redirect(defaultPage)
					.flashing("error" -> "이미 로그인 중입니다."))
		} onlyForWithout block
	
}
