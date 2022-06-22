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
			products <- searchService searchProductsOrderBy(keyword, codeVal, seqVal, pageVal, sizeVal)
		} yield Ok(Json.obj (
			"pageCount" -> productCount/sizeVal,
			"category" -> code,
			"size" -> sizeVal,
			"page" -> pageVal,
			"keyword" -> keyword,
			"products" -> products,
			"sort" -> seqVal
		)))
	}
	
	def getProduct(productId: Int): Action[AnyContent] = Action.async { implicit request =>
		searchService getProductById productId map { product =>
			Ok(Json.toJson(product))
		} getOrElse NotFound recover {
			case _: Exception => BadRequest
		}
	}
	
	def getReviews(productId: Int,
								 size: Option[Int],
								 page: Option[Int]): Action[AnyContent] = Action.async { implicit request =>
		val pageVal = page.getOrElse(1)
		val sizeVal = size.getOrElse(5)
		(for {
			reviews <- communicateService
				.getReviewsByProductIdWithPagination(
					productId = productId,
					size = sizeVal,
					page = pageVal
				)
			reviewCount <- communicateService.getReviewCountsByProductId(productId)
		} yield Ok {
			Json.obj (
				"page" -> pageVal,
				"size" -> sizeVal,
				"pageCount" -> (reviewCount.toFloat / sizeVal.toFloat).ceil,
				"reviews" -> Json.toJson(reviews)
			)
		}) recover {
			case _: NoSuchElementException => NotFound
			case e: Exception => BadRequest(e.getMessage)
		}
	}
	
	def getQnas(productId: Int,
							size: Option[Int],
							page: Option[Int]): Action[AnyContent] = Action.async { implicit request =>
		val pageVal = page.getOrElse(1)
		val sizeVal = size.getOrElse(5)
		(for {
			qnas <- communicateService.getQnasByProductIdWithPagination(
				productId = productId,
				size = sizeVal,
				page = pageVal
			)
			qnaCount <- communicateService.getQnaCountsByProductId(productId)
		} yield Ok {
			Json.obj (
				"page" -> pageVal,
				"size" -> sizeVal,
				"pageCount" -> (qnaCount.toFloat / sizeVal.toFloat).ceil,
				"qnas" -> Json.toJson(qnas)
			)
		}) recover {
			case _: NoSuchElementException => NotFound
			case e: Exception => BadRequest(e.getMessage)
		}
		
	}
	
	
}
