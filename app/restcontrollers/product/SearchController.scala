package restcontrollers.product

import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import services.ProductService
import scala.language.postfixOps
import common.json.CustomJsonApi._
import controllers.Common._
import services.product.{CommunicateService, SearchService}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class SearchController @Inject()(cc: ControllerComponents,
								 searchService: SearchService,
								 communicateService: CommunicateService)
								(implicit ec: ExecutionContext)
		extends AbstractController(cc) {
	
	def search(code: Option[String],
			   keyword: String,
			   page: Option[Int],
			   size: Option[Int],
			   seq: Option[Int]): Action[AnyContent] = Action.async { implicit request =>
		val (codeVal, pageVal, sizeVal, seqVal) =
			(code.getOrElse(""), page.getOrElse(1), size.getOrElse(48), seq.getOrElse(0))
		(for {
			productCount <- searchService getProductCount(keyword, codeVal)
			products <- searchService searchProductsBy(keyword, codeVal, seqVal, pageVal, sizeVal)
		} yield Ok(Json.toJson(products, productCount/sizeVal))) recover {
			case ex:Exception => ex toJsonError
		}
	}
	
	def getProduct(productId: Int): Action[AnyContent] = Action.async { implicit request =>
		searchService getProductById productId map { product =>
			Ok(Json.toJson(product))
		} getOrElse NotFound recover {
			case _: Exception => BadRequest
		}
	}
	
	def getReviews(productId: Int): Action[AnyContent] = Action.async { implicit request =>
		communicateService getReviewsByProductId productId map { reviews =>
			Ok(Json.toJson(reviews))
		} recover {
			case _: NoSuchElementException => NotFound
			case _: Exception => BadRequest
		}
	}
	
}
