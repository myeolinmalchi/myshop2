package controllers.product

import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import services.ProductService
import scala.language.postfixOps
import common.json.CustomJsonApi._
import controllers.Common._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class SearchController @Inject()(cc: ControllerComponents,
								 productService: ProductService)
								(implicit ec: ExecutionContext)
		extends AbstractController(cc) {
	
	def search(code: String,
			   keyword: String,
			   page: Option[Int],
			   size: Option[Int],
			   seq: Option[Int]): Action[AnyContent] = Action.async { implicit request =>
		val codeVal = if(code.equals("0")) "" else code
		val (pageVal, sizeVal, seqVal) = (page.getOrElse(1), size.getOrElse(48), seq.getOrElse(0))
		(for {
			productCount <- productService getProductCount(keyword, codeVal)
			products <- productService searchProductsBy(keyword, codeVal, seqVal, pageVal, sizeVal)
		} yield Ok(views.html.search(products, productCount/sizeVal, pageVal))) recover {
			case ex:Exception => ex toJsonError
		}
	}
	
	def productDetail(productId: Int): Action[AnyContent] = Action.async { implicit request =>
		productService getProductById productId map { product =>
			Ok(views.html.product_detail(product))
		} getOrElse NotFound
	}
	
	def getOneProductOption: Action[AnyContent] = Action.async { implicit request =>
		withAnyJson { value =>
			val productId = (value \ "productId").as[Int]
			val depth = (value \ "depth").as[Int]
			val parentId = (value \ "parentId").as[Int]
			(productService getProductOptionStock(productId, depth, parentId))
					.map (_.filter(_.stock > 0)).getOrError
		}
	}
}
