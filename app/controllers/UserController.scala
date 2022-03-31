package controllers

import dto._
import javax.inject._
import play.api.db.slick._
import play.api.mvc._
import play.api.libs.json._
import play.libs.F.Tuple
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.{Failure, Success, Try}
import services.UserService
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.MySQLProfile
import java.security.MessageDigest
import java.math.BigInteger
import java.time.LocalDateTime
import models.{SellerSessionModel, UserSessionModel}

@Singleton
class UserController @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
							    cc: ControllerComponents)(implicit ec: ExecutionContext)
		extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {
	private val service = new UserService(db)

	implicit val userReads = Json.reads[UserDto]
	implicit val itemReads = Json.reads[ProductOptionItemDto]
	implicit val cartReads = Json.reads[CartDto]
	
	implicit val itemWrites = Json.writes[ProductOptionItemDto]
	implicit val cartWrites = Json.writes[CartDto]
	implicit val addrWrites = Json.writes[AddressDto]
	implicit val userWrites = Json.writes[UserDto]

	private def withSessionUserid(f: String => Future[Result])(implicit request: Request[AnyContent]): Future[Result] =
		request.session.get("userId").map(userId => f(userId)).getOrElse(Future.successful(Ok(Json.toJson(Seq.empty[String]))))

	private def withJsonBody[A](f: A => Future[Result])(implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
		request.body.asJson.map { body =>
			Json.fromJson[A](body) match {
				case JsSuccess(a, path) => f(a)
				case e @JsError(_) => {
					println("error has occurred.")
					Future.successful(Redirect(routes.UserController.registerPage))
				}
				
			}
		}.getOrElse((Future.successful(Redirect(routes.UserController.registerPage))))
	}
	
	private def extractSeller(req: RequestHeader): Future[Option[UserDto]] = {
		val sessionTokenOpt = req.session.get("sessionToken")
		def swap[M](x: Option[Future[M]]): Future[Option[M]] =
			Future.sequence(Option.option2Iterable(x)).map(_.headOption)
		swap (sessionTokenOpt
				.flatMap(token => UserSessionModel.getSession(token))
				.filter(_.expiration.isAfter(LocalDateTime.now()))
				.map(_.sellerId)
				.map(service.getUser))
	}
	
	private def withUser[T](block: UserDto => Future[Result])
							 (implicit request: Request[AnyContent]): Future[Result] =
		extractSeller(request) flatMap {
			case Some(user) => block(user)
			case None => Future(Unauthorized(views.html.seller.no_auth()))
		}
		
	private def withoutUser(block: => Future[Result])
						   (implicit request: Request[AnyContent]): Future[Result] =
		extractSeller(request) flatMap {
			case None => block
			case Some(_) => Future(Unauthorized(views.html.seller.no_auth()))
		}
	
		
	def loginPage = Action.async { implicit request =>
		withoutUser {
			Future(Ok(views.html.login()))
		}
	}
	
	def login = Action.async { implicit request =>
		withoutUser {
			withJsonBody[UserDto] { user =>
				service.login(user.userId, user.userPw).map {
					case Some(able) =>
						if(able) {
							val token = UserSessionModel.generateToken(user.userId, request.session)
							Ok(Json.toJson(true)).withSession("sessionToken" -> token)
						}
						else Ok(Json.toJson(Map("error"->"비밀번호가 일치하지 않습니다.")))
					case None => Ok(Json.toJson(Map("error" -> "존재하지 않는 계정입니다.")))
				}
			}
		}
	}
	
	def registerPage = Action.async { implicit request =>
		withoutUser{ Future(Ok(views.html.register())) }
	}
	
	def register = Action async { implicit request =>
		withJsonBody[UserDto] { user =>
			service.register(user).map {
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
			service.findId(user.email).map {
				case Some(userId) => Ok(Json.toJson(Map("userId" -> userId)))
				case None => Ok(Json.toJson(false))
			}
		}
	}

	def cartList = Action.async { implicit request =>
		withUser[UserDto] { user =>
			service.getCarts(user.userId) transform {
				case Success(result) => Try(Ok(views.html.cart_list(result.toList)))
				case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
			}
		}
	}
	
	def addCart = Action.async { implicit request =>
		withUser[UserDto] { user =>
			withJsonBody[CartDto] {  cart =>
				service.addCart(cart) transform {
					case Success(result) => Try(Redirect(routes.UserController.cartList))
					case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
				}
			}
		}
	}

	def addressList = Action.async { implicit request =>
		withSessionUserid { implicit userId => service.getAddress.map(addr => Ok(Json.toJson(addr))) }
	}

	def addAddress = ???
	
}