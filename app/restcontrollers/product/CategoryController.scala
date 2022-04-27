package restcontrollers.product

import controllers.Common._
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import services.ProductService
import services.product.CategoryService

@Singleton
class CategoryController @Inject() (cc: ControllerComponents,
									categoryService: CategoryService)
								   (implicit ec: ExecutionContext)
		extends AbstractController(cc) {
	
	def getMainCategories: Action[AnyContent] = Action.async { implicit request =>
		categoryService.getMainCategories getOrError
	}
	
	def getChildrens(code: String): Action[AnyContent] = Action.async { implicit request =>
		categoryService.getChildrenCategories(code) getOrError
	}
	
	def getSiblings(code: String): Action[AnyContent] = Action.async { implicit request =>
		categoryService.getSiblingCategories(code) getOrError
	}
}
