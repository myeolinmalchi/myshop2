package controllers

import dto._
import javax.inject._
import play.api.db.slick._
import play.api.mvc._
import play.api.libs.json._
import play.libs.F.Tuple
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.{Failure, Success, Try}
import services.SellerService
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.MySQLProfile
import java.security.MessageDigest
import java.math.BigInteger

@Singleton
class SellerController @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
								cc: ControllerComponents)(implicit ec: ExecutionContext)
		extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {
	private val service = new SellerService(db)
	
	implicit val sellerReads = Json.reads[SellerDto]
	implicit val itemReads = Json.reads[ProductOptionItemDto]
	implicit val optionReads = Json.reads[ProductOptionDto]
	implicit val productReads = Json.reads[ProductDto]
	
	implicit val itemWrites = Json.writes[ProductOptionItemDto]
	implicit val optionWrites = Json.writes[ProductOptionDto]
	implicit val productWrites = Json.writes[ProductDto]
	
	private def withSessionId(f: String => Future[Result])(implicit request: Request[AnyContent]): Future[Result] =
		request.session.get("sellerId").map(sellerId => f(sellerId)).getOrElse(Future.successful(Ok(Json.toJson(Seq.empty[String]))))
	
	
	private def withJsonBody[A](f: A => Future[Result])(implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
		println(request.body.asJson)
		request.body.asJson.map { body =>
			Json.fromJson[A](body) match {
				case JsSuccess(a, path) => f(a)
				case e @JsError(_) => {
					println(e.toString)
					Future.successful(Redirect(routes.SellerController.index))
				}
			}
		}.getOrElse((Future.successful(Redirect(routes.SellerController.index))))
	}
	
	def index = Action { implicit request =>
		request.session.get("sellerId") match {
			case Some(sellerId) => Ok(views.html.seller.index(sellerId))
			case None => Ok(views.html.seller.index(null))
		}
	}
	
	def loginPage = Action { implicit request =>
		Ok(views.html.seller.login())
	}
	
	def login = Action.async { implicit request =>
		withJsonBody[SellerDto] { seller =>
			service.login(seller.sellerId, seller.sellerPw).map {
				case Some(able) =>
					if(able) Ok(Json.toJson(true)).withSession("sellerId" -> seller.sellerId)
					else Ok(Json.toJson(Map("error"->"비밀번호가 일치하지 않습니다.")))
				case None => Ok(Json.toJson(Map("error" -> "존재하지 않는 계정입니다.")))
			}
		}
	}
	
	def registerPage = Action { implicit request =>
		Ok(views.html.seller.register())
	}
	
	def register = Action async { implicit request =>
		withJsonBody[SellerDto] { seller =>
			service.register(seller).map {
				case Some(e) => Ok(Json.toJson(Map("error" -> e)))
				case None => Ok(Json.toJson(true))
			}
		}
	}
	
	def logout = Action { implicit request =>
		Redirect(routes.SellerController.index).withSession(request.session - "sellerId")
	}
	
	def findSellerPage = Action { Ok(views.html.findUser()) }
	
	def findId = Action.async { implicit request =>
		withJsonBody[SellerDto] { seller =>
			service.findId(seller.email).map {
				case Some(sellerId) => Ok(Json.toJson(Map("sellerId" -> sellerId)))
				case None => Ok(Json.toJson(false))
			}
		}
	}
	
	def productRegisterPage = Action.async { implicit request =>
		withSessionId { sellerId =>
			Future(Ok(views.html.seller.product.register(sellerId)))
		}
	}
	
	def addProduct = Action.async { implicit request =>
		withSessionId { implicit sellerId =>
			withJsonBody[ProductDto] { implicit product =>
				service.addProduct transform {
					case Success(_) => Try(Ok(Json.toJson(true)))
					case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
				}
			}
		}
	}
	
	def getProduct = Action.async { implicit request =>
		withSessionId { implicit sellerId =>
			service.getProductList transform {
				case Success(products) => Try(Ok(Json.toJson(products)))
				case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
			}
		}
	}
}
