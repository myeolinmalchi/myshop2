package controllers

import dto._
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import services.ProductService
import slick.jdbc.JdbcProfile
import javax.inject._
import play.api.db.slick._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class CategoryController @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
								cc: ControllerComponents)(implicit ec: ExecutionContext)
		extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile]{
	private val productService = new ProductService(db)
	
	implicit val itemReads = Json.reads[ProductOptionItemDto]
	implicit val imageRead = Json.reads[ProductImageDto]
	implicit val optionReads = Json.reads[ProductOptionDto]
	implicit val productReads = Json.reads[ProductDto]
	
	implicit val itemWrites = Json.writes[ProductOptionItemDto]
	implicit val imageWrite = Json.writes[ProductImageDto]
	implicit val optionWrites = Json.writes[ProductOptionDto]
	implicit val productWrites = Json.writes[ProductDto]
	
	private def withJsonBody[A](f: A => Future[Result])(implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
		println(request.body.asJson)
		request.body.asJson.map { body =>
			Json.fromJson[A](body) match {
				case JsSuccess(a, path) => f(a)
				case e@JsError(_) => {
					println(e.toString)
					Future.successful(Redirect(routes.SellerController.index))
				}
			}
		}.getOrElse((Future.successful(Redirect(routes.SellerController.index))))
	}
	
	def getMainCategories = Action.async { implicit request =>
		productService.getMainCategories transform {
			case Success(cats) => Try(Ok(Json.toJson(cats)))
			case Failure(e) => Try(Ok(Json.toJson(false)))
		}
	}
		
	def getChildrens = Action.async { implicit request =>
		withJsonBody[String] { code =>
			productService.getChildrenCategories(code) transform {
				case Success(cats) => Try(Ok(Json.toJson(cats)))
				case Failure(e) => {
					e.printStackTrace()
					Try(Ok(Json.toJson(false)))
				}
			}
		}
	}
	
	def getSiblings = Action.async { implicit request =>
		withJsonBody[String] { code =>
			productService.getSiblingCategories(code) transform {
				case Success(cats) => Try(Ok(Json.toJson(cats)))
				case Failure(e) => Try(Ok(Json.toJson(false)))
			}
		}
	}
}
