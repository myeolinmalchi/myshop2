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
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
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
    
  def index = Action { implicit request =>
      request.session.get("userId") match {
        case Some(userId) => Ok(views.html.index(userId))
        case None => Ok(views.html.index(null))
      }
  }
    
    def search(code: String, keyword: String) = Action.async { implicit request =>
        productService searchProducts(keyword, code) transform {
            case Success(products) => Try(Ok(Json.toJson(products)))
            case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
        }
    }
}
