package controllers.user

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import scala.concurrent.ExecutionContext
import controllers.user.CommonApi._
import common.json.CustomJsonApi._
import controllers.Common.CustomFuture
import scala.language.postfixOps
import services.user._

@Singleton
class OrderController @Inject()(cc: ControllerComponents)
							   (implicit ec: ExecutionContext,
								accountService: AccountService,
								orderService: OrderService)
		extends AbstractController(cc) {
	
	def order: Action[AnyContent]= Action.async { implicit request =>
		withUser { user =>
			withAnyJson { value =>
				val userId = user.userId
				val cartIdList = (value \ "cartIdList").as[List[Int]]
				orderService.newOrder(userId, cartIdList) trueOrError
			}
		} endWith
	}
	
	def getOrders: Action[AnyContent]= Action.async { implicit request =>
		withUser { user =>
			orderService.getOrderByUserId(user.userId) getOrError
		} endWith
	}
	
	def checkUserOrderedThisProduct: Action[AnyContent]= Action.async { implicit request =>
		withUser { user =>
			withAnyJson { value =>
				val productId = (value \ "productId").as[Int]
				orderService.checkUserOrderedThisProduct(user.userId, productId) trueOrError
			}
		} endWith
	}
	
}
