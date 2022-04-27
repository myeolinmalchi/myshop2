package controllers.seller

import common.json.CustomJsonApi._
import controllers.seller.CommonApi._
import dto.{ProductDto, StockDto}
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, Call, ControllerComponents}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import services.{ProductService, SellerService}
import controllers.routes
import scala.language.postfixOps
import controllers.Common._

class ProductController @Inject()(cc: ControllerComponents)
								 (implicit ec: ExecutionContext,
									   sellerService: SellerService,
									   productService: ProductService)
		extends AbstractController(cc) {
	
	def productRegisterPage: Action[AnyContent] = Action.async { implicit request =>
		withSeller { seller =>
			productService.getMainCategories flatMap { categories =>
				Future(Ok(views.html.seller.product.register(categories, seller.sellerId)))
			}
		}
	}
	
	def addProduct: Action[AnyContent] = Action.async { implicit request =>
		withSeller { seller =>
			withJsonDto[ProductDto] { product =>
				sellerService.addProduct(seller.sellerId, product) trueOrError
			}
		}
	}
	
	def getProducts: Action[AnyContent] = Action.async { implicit request =>
		withSeller { seller =>
			sellerService getProductList seller.sellerId map { products =>
				Ok(views.html.seller.product.product_list(products))
			} recover {
				case ex: Exception => ex toJsonError
			}
		}
	}
	
	def getAllProductStock: Action[AnyContent] = Action.async { implicit request =>
		withSeller { _ =>
			withJsonDto[StockDto] { stock =>
				sellerService.getProductStock(stock.productId) getOrError
			}
		}
	}
	
	def updateStock: Action[AnyContent] = Action.async { implicit request =>
		withSeller { _ =>
			withJsonDto[StockDto] { stock =>
				sellerService.updateStock(stock.productStockId, stock.stock) getOrError
			}
		}
	}
}
