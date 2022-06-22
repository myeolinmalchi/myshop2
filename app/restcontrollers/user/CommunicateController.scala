package restcontrollers.user

import cats.data.OptionT
import dto.{QnaRequestDto, ReviewRequestDto}
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import restcontrollers.Common.withJson
import scala.concurrent.ExecutionContext
import services.user.{AccountService, AuthService, OrderService}
import services.product.CommunicateService

class CommunicateController @Inject()(cc: ControllerComponents)
																		 (implicit ec: ExecutionContext,
																			accountService: AccountService,
																			authService: AuthService,
																			orderService: OrderService,
																			commService: CommunicateService)
	extends AbstractController(cc) {
	
	import authService.withUser
	
	def recentlyOrderedProductInfo(userId: String, productId: Int): Action[AnyContent] = Action.async { implicit request =>
		withUser(userId) { _ =>
			OptionT(commService.getRecentlyOrderedProduct(userId, productId)) map { productInfo =>
				Ok(Json.toJson(productInfo))
			} getOrElse NotFound
		}
	}
	
	def writeReview(userId: String): Action[AnyContent] = Action.async { implicit request =>
		withJson[ReviewRequestDto] { review =>
			withUser(userId)	 { _ =>
				(for {
					_ <- orderService.checkUserOrderedThisProduct(review.userId, review.productId)
					aff <- commService.addReview(review)
				} yield Created) recover {
					case e: Exception => BadRequest(e.getMessage)
				}
				
			}
		}
	}
	
	def writeQna(userId: String): Action[AnyContent] = Action.async { implicit request =>
		withJson[QnaRequestDto] { qna =>
			withUser(userId) { _ =>
				for {
					aff <- commService.addQna(qna)
				} yield if (aff == 1) Created
				else BadRequest
			}
		}
	}
	
}
