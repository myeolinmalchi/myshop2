package restcontrollers.seller

import common.json.CustomJsonApi._
import controllers.seller.CommonApi._
import dto.{ProductDto, StockDto}
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, Call, ControllerComponents, Result}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import services.SellerService
import controllers.routes
import scala.language.postfixOps
import controllers.Common._
import restcontrollers.Common.withJson
import restcontrollers.seller.Common.SellerAuth
import services.product.SearchService
import services.seller.{AccountService, ProductService}

class ProductController @Inject()(cc: ControllerComponents)
								 (implicit ec: ExecutionContext,
								  productService: ProductService,
								  searchService: SearchService,
								  accountService: AccountService)
		extends AbstractController(cc) {
	
	private def checkOwnProduct(sellerId: String, productId: Int)
							   (result: => Future[Result]): Future[Result]= {
		productService checkOwnProduct(sellerId, productId) flatMap {
			case true => result
			case false => Future(BadRequest)
		}
	}
	
	def addProduct(sellerId: String): Action[AnyContent] = Action.async { implicit request =>
		SellerAuth(sellerId).auth { _ =>
			withJson[ProductDto] { product =>
				productService.addProduct(product).map { _ =>
					Ok(Json.toJson(true))
				}.recover {
					case e: Exception =>
						e.printStackTrace()
						Ok(Json.toJson(e.getMessage))
				}
			}
		}
	}
	
	def getProducts(sellerId: String): Action[AnyContent] = Action.async { implicit request =>
		SellerAuth(sellerId).auth { _ =>
			productService getProductList sellerId map { products =>
				Ok(Json.toJson(products))
			} recover {
				case ex: Exception => ex toJsonError
			}
		}
	}
	
	def getAllProductStock(sellerId: String, productId: Int): Action[AnyContent] = Action.async { implicit request =>
		SellerAuth(sellerId).auth { _ =>
			checkOwnProduct(sellerId, productId) {
				productService.getProductStock(productId) getOrError
			}
		}
	}
	
	def updateStock(sellerId: String, productId: Int): Action[AnyContent] = Action.async { implicit request =>
		SellerAuth(sellerId).auth { _ =>
			checkOwnProduct(sellerId, productId) {
				withAnyJson { value =>
					val stockId = (value \ "stockId").as[Int]
					val stock = (value \ "stock").as[Int]
					productService checkProductOwnStock (productId, stockId) flatMap {
						case true => productService.updateStock(stockId, stock) getOrError
						case false => Future(BadRequest)
					}
				}
			}
		}
	}
}
