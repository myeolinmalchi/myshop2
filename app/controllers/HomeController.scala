package controllers

import dto._
import javax.inject._
import play.api.db.slick._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.ExecutionContext
import services.ProductService
import slick.jdbc.JdbcProfile

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (cc: ControllerComponents,
                                productService: ProductService)
                               (implicit ec: ExecutionContext)
        extends AbstractController(cc) {
    implicit val itemReads = Json.reads[ProductOptionItemDto]
    implicit val imageRead = Json.reads[ProductImageDto]
    implicit val optionReads = Json.reads[ProductOptionDto]
    implicit val productReads = Json.reads[ProductDto]
    
    implicit val itemWrites = Json.writes[ProductOptionItemDto]
    implicit val imageWrite = Json.writes[ProductImageDto]
    implicit val optionWrites = Json.writes[ProductOptionDto]
    implicit val productWrites = Json.writes[ProductDto]
    
  def index = Action { implicit request =>
      Ok(views.html.index())
  }
}
