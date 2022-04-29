package restcontrollers.user

import controllers.Common.CustomFuture
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import restcontrollers.Common.withAnyJson
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import services.user.{AccountService, AuthService, OrderService}

@Singleton
class OrderController @Inject()(cc: ControllerComponents)
							   (implicit ec: ExecutionContext,
								accountService: AccountService,
								authService: AuthService,
								orderService: OrderService)
		 extends AbstractController(cc) {
	
	import authService.withUserAuth
	
	def createOrder(userId: String): Action[AnyContent] = Action.async { implicit request  =>
		withUserAuth(userId) { _ =>
			withAnyJson { value =>
				val cartIdList = (value \ "cartIdList").as[List[Int]]
				orderService.newOrder(userId, cartIdList) trueOrError
			}
		}
	}
	
	def getOrders(userId: String): Action[AnyContent] =Action.async { implicit request =>
		withUserAuth(userId) { _ =>
			orderService.getOrderByUserId(userId) getOrError
		}
	}
	
	def checkUserOrdered(userId: String, productId: Int): Action[AnyContent] = Action.async { implicit request =>
		withUserAuth(userId) { _ =>
			orderService.checkUserOrderedThisProduct(userId, productId) trueOrError
		}
	}
}
