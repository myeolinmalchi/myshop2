package controllers

import dto._
import java.time.LocalDateTime
import javax.inject._
import models.{NonUserModel, UserSessionModel}
import play.api.Logging
import play.api.db.slick._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import services.{NonUserService, UserService, UserServiceImpl}
import slick.jdbc.JdbcProfile

@Singleton
class UserController @Inject() (cc: ControllerComponents,
								userService: UserService,
								nonUserService: NonUserService)
							   (implicit ec: ExecutionContext)
		extends AbstractController(cc) with Logging{
	
	private implicit val defaultPage = routes.HomeController.index
	
	private object InnerApi {
		// request body의 json 데이터에서 dto를 추출한다.
		def withJsonDto[A](f: A => Future[Result])
						  (implicit request: Request[AnyContent],
						   reads: Reads[A], defaultPage: Call): Future[Result] = {
			request.body.asJson.map { body =>
				Json.fromJson[A](body) match {
					case JsSuccess(a, path) => f(a)
					case e @JsError(_) =>
						logger.info(s"Invalid request body detected: ${e.toString}")
						Future.successful(Redirect(defaultPage))
				}
			}.getOrElse(Future.successful(Redirect(defaultPage)))
		}
		
		def withAnyJson(f: JsValue => Future[Result])
					   (implicit request: Request[AnyContent]): Future[Result] = {
			request.body.asJson match {
				case Some(value) => f(value)
				case None =>
					logger.info(s"No request body detected")
					Future(Ok(Json.toJson(Map("error" -> "올바르지 않은 요청입니다."))))
			}
		}
		
		// request 에서 사용자 토큰을 추출하여 UserSessionModel에서 체크한다.
		private def extractUser(req: RequestHeader): Future[Option[UserDto]] = {
			val sessionTokenOpt = req.session.get("sessionToken")
			def swap[M](x: Option[Future[M]]): Future[Option[M]] =
				Future.sequence(Option.option2Iterable(x)).map(_.headOption)
			swap (sessionTokenOpt
					.flatMap(token => UserSessionModel.getSession(token))
					.filter(_.expiration.isAfter(LocalDateTime.now()))
					.map(_.userId)
					.map(userService.getUser))
		}
		
		
		case class WithUser(block: UserDto => Future[Result])
						   (implicit request: Request[AnyContent]){
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
					case None => Future(Redirect(routes.UserController.loginPage)
							.flashing("error" -> "로그인이 필요합니다.")
							.withSession(request.session - "sessionToken"))
				}
		}
		object WithUser {
			def apply(block: UserDto => Future[Result])
					 (implicit request: Request[AnyContent]) = new WithUser(block)
		}
		
		// 쿠키에서 비회원 토큰을 조회(없을경우 생성)하여 동작을 수행한다.
		// withUser 메서드와 함께 사용한다.
		// 쿠키는 일주일(604800초)동안 유지된다.
		def withNonUserToken(f: String => Future[Result])
							(implicit request: Request[AnyContent]): Future[Result] =
			request.cookies.get("idToken") match {
				case Some(idToken) => f(idToken.value)
				case None => {
					val token = NonUserModel.generateToken
					f(token) map(_.withCookies(
						new Cookie("idToken", token, Some(604800), httpOnly = false)))
				}
			}
			
		def withUser(block: UserDto => Future[Result])
					(implicit request: Request[AnyContent]): WithUser = WithUser(block)
		
		def withoutUser(block: => Future[Result])
					   (implicit request: Request[AnyContent]): Future[Result] =
			WithUser {
				_ => Future(Redirect(routes.HomeController.index)
						.flashing("error" -> "이미 로그인 중입니다."))
			} onlyForWithout block
		
		implicit class CustomFuture[T](a: Future[T]) {
			private def orError(f: T => Result): Future[Result] =
				a transform {
					case Success(result) => Try(f(result))
					case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
				}
				
			def trueOrError: Future[Result] = orError { _ =>
				Ok(Json.toJson(true))
			}
			
			def getOrError(implicit w: Writes[T]): Future[Result] = orError { result =>
				Ok(Json.toJson(result))
			}
		}
		
		// List[CartDto]를 담은 퓨처가 정상적으로 실행되면 장바구니 페이지로 이동, 아니면 에러메세지
		def cartOrError(result: Future[List[CartDto]])
					   (implicit request: Request[AnyContent]): Future[Result] = result transform {
			case Success(result) => Try(Ok(views.html.cart_list(result)))
			case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
		}
	}
	
	import InnerApi._

	def loginPage = Action.async { implicit request =>
		withoutUser { Future(Ok(views.html.login())) }
	}
	
	def login = Action.async { implicit request => withoutUser {
		withJsonDto[UserDto] { user =>
			(userService login (user.userId, user.userPw)) transform {
				case Success(_) =>
					val token = UserSessionModel.generateToken(user.userId, request.session)
					Try(Ok(Json.toJson(true)).withSession("sessionToken" -> token))
				case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
			}
		}
	}}
	
	def registerPage = Action.async { implicit request =>
		withoutUser{
			Future(Ok(views.html.register()))
		}
	}
	
	def register = Action async { implicit request =>
		withJsonDto[UserDto] { user =>
			userService.register(user) trueOrError
		}
	}
	
	def logout = Action.async { implicit request =>
		withUser { _ =>
			UserSessionModel.remSession(request.session)
			Future(Redirect(routes.HomeController.index).withSession(request.session - "sessionToken"))
		} endWith
	}
	
	def findUserPage = Action { Ok(views.html.findUser()) }
	
	def findId = Action.async { implicit request =>
		withJsonDto[UserDto] { user =>
			userService.findId(user.email).map {
				case Some(userId) => Ok(Json.toJson(Map("userId" -> userId)))
				case None => Ok(Json.toJson(false))
			}
		}
	}
	
	def cartList = Action.async { implicit request =>
		withUser { user =>
			cartOrError(userService.getCarts(user.userId))
		} ifNot withNonUserToken { token =>
			cartOrError(nonUserService.getCarts(token))
		}
	}
	
	def addCart = Action.async { implicit request =>
		withJsonDto[CartDto] { cart =>
			withUser { _ =>
				userService.addCart(cart) trueOrError
			} ifNot withNonUserToken { _ =>
				nonUserService.addCart(cart) trueOrError
			}
		}
	}
	
	def deleteCart = Action.async { implicit request =>
		withAnyJson { value =>
			val cartId = (value \ "cartId").as[Int]
			withUser { _ =>
				userService.deleteCart(cartId) trueOrError
			} ifNot withNonUserToken { _ =>
				nonUserService.deleteCart(cartId) trueOrError
			}
		}
	}
	
	def updateCartQuantity = Action.async { implicit request =>
		withAnyJson { value =>
			val quantity = (value \ "quantity").as[Int]
			val cartId = (value \ "cartId").as[Int]
			withUser { _ =>
				userService.updateQuantity(quantity)(cartId) trueOrError
			} ifNot withNonUserToken { _ =>
				nonUserService.updateQuantity(quantity)(cartId) trueOrError
			}
		}
	}
	
	def order = Action.async { implicit request =>
		withUser { user =>
			withAnyJson { value =>
				val userId = user.userId
				val cartIdList = (value \ "cartIdList").as[List[Int]]
				userService.newOrder(userId, cartIdList) trueOrError
			}
		} endWith
	}
	
	
	def getOrders = Action.async { implicit request =>
		withUser { user =>
			userService.getOrderByUserId(user.userId) getOrError
		} endWith
	}
	
	def checkUserOrderedThisProduct = Action.async { implicit request =>
		withUser { user =>
			withAnyJson { value =>
				val productId = (value \ "productId").as[Int]
				userService.checkUserOrderedThisProduct(user.userId, productId) getOrError
			}
		} endWith
	}
	
	
	def addressList = ???

	def addAddress = ???
	
}