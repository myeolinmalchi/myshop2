package controllers

import dto._
import javax.inject._
import play.api.db.slick._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import services.ProductService
import slick.jdbc.JdbcProfile

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class SearchController @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
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
	
	def search(code: String, keyword: String,
			   page: Option[Int], size: Option[Int], seq: Option[Int]) = Action.async { implicit request =>
			
		val codeVal = if(code.equals("0")) "" else code
		def intOp(x: Option[Int], default: Int) = x match {
			case Some(value) => value
			case None => default
		}
		val pageVal = intOp(page, 1)
		val sizeVal = intOp(size, 48)
		val seqVal = intOp(seq, 0)
		
		productService getProductCount(keyword, codeVal) transform {
			case Success(count) => Try(count)
			case Failure(e) => Failure(e)
		} flatMap { productNum =>
			val st = System.nanoTime()
			productService searchProductsBy(keyword, codeVal, seqVal, pageVal, sizeVal) transform{
				case Success(products) => Try(Ok(views.html.search(products, productNum/sizeVal+1,pageVal)))
				case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
			}
		}
	}
	
	def productDetail(productId: Int) = Action.async { implicit request =>
		productService getProductOptionStock(productId, 0, 0) transform {
			case Success(stocks) => Try(stocks)
			case Failure(e) => Failure(e)
		} flatMap { stocks =>
			productService.getProductById(productId).transform {
				case Success(result) => Try(Ok(views.html.product_detail(result)))
				case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
			}
		}
	}
	
	def getOneProductOption = Action.async { implicit request =>
		request.body.asJson match {
			case Some(result) => {
				val productId = (result \ "productId").as[Int]
				val depth = (result \ "depth").as[Int]
				val parentId = (result \ "parentId").as[Int]
				productService getProductOptionStock(productId, depth, parentId) transform {
					case Success(stocks) => Try(Ok(Json.toJson(stocks.filter(_.stock>0))))
					case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
				}
			}
			case None => Future(Ok(Json.toJson(Map("error" -> "올바르지 않은 요청입니다."))))
		}
	}
}
