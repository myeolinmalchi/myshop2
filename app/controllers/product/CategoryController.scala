package controllers.product

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, Call, ControllerComponents}
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}
import services.ProductService
import common.json.CustomJsonApi._
import controllers.routes
import scala.language.postfixOps
import controllers.Common._

@Singleton
class CategoryController @Inject() (cc: ControllerComponents,
										productService: ProductService)
									   (implicit ec: ExecutionContext)
		extends AbstractController(cc) {
	
	private implicit val defaultPage: Call = controllers.user.routes.IndexController.index
	
	def getMainCategories: Action[AnyContent] = Action.async { implicit request =>
		productService.getMainCategories getOrError
	}
	
	def getChildrens: Action[AnyContent] = Action.async { implicit request =>
		withJsonDto[String] { code =>
			productService.getChildrenCategories(code) getOrError
		}
	}
	
	def getSiblings: Action[AnyContent] = Action.async { implicit request =>
		withJsonDto[String] { code =>
			productService.getSiblingCategories(code) getOrError
		}
	}
}
