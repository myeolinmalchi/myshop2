package restcontrollers.user

import controllers.Common.{CustomException, CustomFuture}
import restcontrollers.user.Common._
import restcontrollers.Common._
import dto.CartDto
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import services.user._
import services.{NonUserService, UserService}

@Singleton
class CartController @Inject()(cc: ControllerComponents)
							  (implicit ec: ExecutionContext,
							   userService: UserService,
							   accountService: AccountService,
							   cartService: CartService,
							   orderService: OrderService,
							   nonUserService: NonUserService)
		extends AbstractController(cc) {
	
	private def checkOwnCart(userId: String, cartId: Int)
							(result: => Future[Result]): Future[Result] =
		cartService checkOwnCart (userId, cartId) flatMap {
			case true => result
			case false => Future(BadRequest)
		}
	
	def getCart(userId: String, cartId: Int): Action[AnyContent] = Action.async { implicit request =>
		UserAuth(userId).auth { _ =>
			checkOwnCart(userId, cartId) {
				cartService.getCart(cartId) getOrError
			}
		}
	}
	
	def getCarts(userId: String): Action[AnyContent] = Action.async { implicit request =>
		UserAuth(userId).auth { implicit user =>
			cartService.getCarts getOrError
		}
	}
	
	def addCart(userId: String): Action[AnyContent] = Action.async { implicit request =>
		UserAuth(userId).auth { _ =>
			withJson[CartDto] { implicit cart =>
				cartService.addCart trueOrError
			}
		}
	}
	
	def deleteCart(userId: String, cartId: Int): Action[AnyContent] = Action.async { implicit request =>
		UserAuth(userId).auth { _ =>
			checkOwnCart(userId, cartId) {
				cartService.deleteCart(cartId) trueOrError
			}
		}
	}
	
	def updateQuantity(userId: String, cartId: Int): Action[AnyContent] = Action.async { implicit request =>
		UserAuth(userId).auth { _ =>
			checkOwnCart(userId, cartId) {
				withAnyJson { value =>
					val quantity = (value \ "quantity").as[Int]
					cartService.updateQuantity2(cartId, quantity) trueOrError
				}
			}
		}
	}
}
