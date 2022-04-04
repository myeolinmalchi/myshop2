package controllers

import dto._
import javax.inject._
import play.api.db.slick._
import play.api.mvc._
import play.api.libs.json._
import play.libs.F.Tuple
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.{Failure, Success, Try}
import services.{NonUserService, UserService}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.MySQLProfile
import java.security.MessageDigest
import java.math.BigInteger
import java.time.LocalDateTime
import models.{NonUserModel, SellerSessionModel, UserSessionModel}

@Singleton
class UserController @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
							    cc: ControllerComponents)(implicit ec: ExecutionContext)
		extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {
	
	private val userService = new UserService(db)
	private val nonUserService = new NonUserService(db)
	
	private object InnerApi {
		// request body의 json 데이터에서 dto를 추출한다.
		def withJsonBody[A](f: A => Future[Result])
						   (implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
			request.body.asJson.map { body =>
				Json.fromJson[A](body) match {
					case JsSuccess(a, path) => f(a)
					case e @JsError(_) => {
						println(s"error has occurred: ${e.toString}")
						Future.successful(Redirect(routes.UserController.loginPage))
					}
				}
			}.getOrElse((Future.successful(Redirect(routes.UserController.loginPage))))
		}
		
		// request 에서 사용자 토큰을 추출하여 UserSessionModel에서 체크한다.
		def extractUser(req: RequestHeader): Future[Option[UserDto]] = {
			val sessionTokenOpt = req.session.get("sessionToken")
			def swap[M](x: Option[Future[M]]): Future[Option[M]] =
				Future.sequence(Option.option2Iterable(x)).map(_.headOption)
			swap (sessionTokenOpt
					.flatMap(token => UserSessionModel.getSession(token))
					.filter(_.expiration.isAfter(LocalDateTime.now()))
					.map(_.userId)
					.map(userService.getUser))
		}
		
		// 유저 권한이 없으면 로그인 페이지로 즉시 이동하며 에러메세지를 보낸다.
		def withUser[T](block: UserDto => Future[Result])
					   (implicit request: Request[AnyContent]): Future[Result] =
			extractUser(request) flatMap {
				case Some(user) => block(user)
				case None => Future(Redirect(routes.UserController.loginPage)
						.flashing("error" -> "로그인이 필요합니다.")
						.withSession(request.session - "sessionToken"))
			}
		
		// 유저 권한이 없으면 다른 동작을 한다.
		def withUserOr[T](block: UserDto => Future[Result])(result: => Future[Result])
						 (implicit request: Request[AnyContent]): Future[Result] =
			extractUser(request) flatMap {
				case Some(user) => block(user)
				case None => result.map(_.withSession(request.session - "sessionToken"))
			}
		
		// 유저 권한이 있으면 메인 페이지로 즉시 이동하며 에러메세지를 보낸다.
		def withoutUser(block: => Future[Result])
					   (implicit request: Request[AnyContent]): Future[Result] =
			extractUser(request) flatMap {
				case None => block
				case Some(_) =>
					Future(Redirect(routes.HomeController.index)
							.flashing("error" -> "이미 로그인 중입니다."))
			}
		
		// 쿠키에서 비회원 토큰을 조회(없을경우 생성)하여 동작을 수행한다.
		// withUserOr 메서드와 함께 사용한다.
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
		
		// 퓨처가 성공적으로 수행되면 true를, 아니라면 에러메세지를 반환한다.
		def trueOrError(result: Future[_]) =
			result transform {
				case Success(result) => Try(Ok(Json.toJson(true)))
				case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
			}
			
		// List[CartDto]를 담은 퓨처가 정상적으로 실행되면 장바구니 페이지로 이동, 아니면 에러메세지
		def cartOrError(result: Future[List[CartDto]])
					   (implicit request: Request[AnyContent]) = result transform {
			case Success(result) => Try(Ok(views.html.cart_list(result)))
			case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
		}
	}
	
	import InnerApi._

	def loginPage = Action.async { implicit request =>
		withoutUser { Future(Ok(views.html.login())) }
	}
	
	def login = Action.async { implicit request => withoutUser {
		withJsonBody[UserDto] { user =>
			userService.login(user.userId, user.userPw).map {
				case Some(able) if able => {
					val token = UserSessionModel.generateToken(user.userId, request.session)
					Ok(Json.toJson(true)).withSession("sessionToken" -> token) }
				case None => Ok(Json.toJson(Map("error" -> "존재하지 않는 계정입니다.")))
				case _ => Ok(Json.toJson(Map("error"->"비밀번호가 일치하지 않습니다.")))
			}
		}
	}}
	
	def registerPage = Action.async { implicit request =>
		withoutUser{ Future(Ok(views.html.register())) }
	}
	
	def register = Action async { implicit request =>
		withJsonBody[UserDto] { user =>
			userService.register(user).map {
				case Some(e) => Ok(Json.toJson(Map("error" -> e)))
				case None => Ok(Json.toJson(true))
			}
		}
	}
	
	def logout = Action.async { implicit request =>
		withUser[UserDto] { user =>
			UserSessionModel.remSession(request.session)
			Future(Redirect(routes.HomeController.index).withSession(request.session - "sessionToken"))
		}
	}
	
	def findUserPage = Action { Ok(views.html.findUser()) }
	
	def findId = Action.async { implicit request =>
		withJsonBody[UserDto] { user =>
			userService.findId(user.email).map {
				case Some(userId) => Ok(Json.toJson(Map("userId" -> userId)))
				case None => Ok(Json.toJson(false))
			}
		}
	}
	
	def cartList = Action.async { implicit request =>
		withUserOr[UserDto] { user => cartOrError(userService.getCarts(user.userId)) }
		{withNonUserToken { token => cartOrError(nonUserService.getCarts(token)) }}
	}
	
	def addCart = Action.async { implicit request =>
		withJsonBody[CartDto] { cart =>
			withUserOr[UserDto] { user => trueOrError(userService.addCart(cart)) }
			{withNonUserToken { token => trueOrError(nonUserService.addCart(cart)) }}
		}
	}
	
	def deleteCart = Action.async { implicit request =>
		request.body.asJson match {
			case Some(value) =>
				val cartId = (value \ "cartId").as[Int]
				withUserOr[UserDto] { user => trueOrError(userService.deleteCart(cartId)) }
				{withNonUserToken { token => trueOrError(nonUserService.deleteCart(cartId)) }}
			case None => Future(Ok(Json.toJson(Map("error" -> "올바르지 않은 요청입니다."))))
		}
	}
	
	def updateCartQuantity = Action.async { implicit request =>
		request.body.asJson match {
			case Some(value) =>
				val quantity = (value \ "quantity").as[Int]
				val cartId = (value \ "cartId").as[Int]
				withUserOr[UserDto] { user => trueOrError(userService.updateQuantity(quantity)(cartId)) }
				{withNonUserToken { token => trueOrError(nonUserService.updateQuantity(quantity)(cartId)) }}
			case None => Future(Ok(Json.toJson(Map("error" -> "올바르지 않은 요청입니다."))))
		}
	}
	
	def order = Action.async { implicit request =>
		withUser[UserDto] { user =>
			request.body.asJson match {
				case Some(value) =>
					val userId = (value \ "userId").as[String]
					val cartIdList = (value \ "cartIdList").as[List[Int]]
					trueOrError(userService.newOrder(userId, cartIdList))
			}
		}
	}
	
	def getOrders = Action.async { implicit request =>
		withUser[UserDto] { user =>
			userService.getOrderByUserId(user.userId) transform {
				case Success(result) => Try(Ok(Json.toJson(result)))
				case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
			}
		}
	}
	
	
	def addressList = ???

	def addAddress = ???
	
}