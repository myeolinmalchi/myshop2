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

@Singleton
class UserController @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
							    cc: ControllerComponents)(implicit ec: ExecutionContext)
		extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {
	private val service = new UserService(db)

	implicit val userReads = Json.reads[UserDto]
	implicit val cartWrites = Json.writes[CartDto]
	implicit val addrWrites = Json.writes[AddressDto]

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
	

	def loginPage = Action { implicit request =>
		Ok(views.html.login())
	}
	
	def login = Action.async { implicit request =>
		withJsonBody[UserDto] { user =>
			println("userId: "+user.userId)
			service.login(user.userId, user.userPw).map {
				case Some(able) =>
					if(able) Ok(Json.toJson(true)).withSession("userId" -> user.userId)
					else Ok(Json.toJson(Map("error"->"비밀번호가 일치하지 않습니다.")))
				case None => Ok(Json.toJson(Map("error" -> "존재하지 않는 계정입니다.")))
			}
		}
	}
	
	def registerPage = Action { implicit request =>
		Ok(views.html.register())
	}
	
	def register = Action async { implicit request =>
		withJsonBody[UserDto] { user =>
			service.register(user).map {
				case Some(e) => Ok(Json.toJson(Map("error" -> e)))
				case None => Ok(Json.toJson(true))
			}
		}
	}
	
	def logout = Action { implicit request =>
		Redirect(routes.HomeController.index).withSession(request.session - "userId")
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
		withSessionUserid { implicit userId => service.getCarts.map(cart => Ok(Json.toJson(cart))) }
	}
	
	def addCart = ???

	def addressList = Action.async { implicit request =>
		withSessionUserid { implicit userId => service.getAddress.map(addr => Ok(Json.toJson(addr))) }
	}

	def addAddress = ???
	
}