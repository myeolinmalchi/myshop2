package restcontrollers.user

import controllers.Common.{CustomException, CustomFuture}
import services.user.AuthService._
import restcontrollers.Common._
import dto.CartDto
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import services.user._
import services.{NonUserService}

@Singleton
class CartController @Inject()(cc: ControllerComponents)
							  (implicit ec: ExecutionContext,
							   accountService: AccountService,
							   cartService: CartService,
							   orderService: OrderService,
							   authService: AuthService,
							   nonUserService: NonUserService)
		extends AbstractController(cc) {
	
	import authService.withUserAuth
	
	private def checkOwnCart(userId: String, cartId: Int)
							(result: => Future[Result]): Future[Result] =
		cartService checkOwnCart (userId, cartId) flatMap {
			case true => result
			case false => Future(BadRequest)
		}
	
	def getCart(userId: String, cartId: Int): Action[AnyContent] = Action.async { implicit request =>
		withUserAuth(userId) { _ =>
			checkOwnCart(userId, cartId) {
				cartService.getCart(cartId) getOrError
			}
		}
	}
	
	def getCarts(userId: String): Action[AnyContent] = Action.async { implicit request =>
		withUserAuth(userId) { implicit user =>
			cartService.getCarts(userId) getOrError
		}
	}
	
	def addCart(userId: String): Action[AnyContent] = Action.async { implicit request =>
		withUserAuth(userId) { _ =>
			withJson[CartDto] { implicit cart =>
				cartService.addCart trueOrError
			}
		}
	}
	
	def deleteCart(userId: String, cartId: Int): Action[AnyContent] = Action.async { implicit request =>
		withUserAuth(userId) { _ =>
			checkOwnCart(userId, cartId) {
				cartService.deleteCart(cartId) trueOrError
			}
		}
	}
	
	def updateQuantity(userId: String, cartId: Int): Action[AnyContent] = Action.async { implicit request =>
		withUserAuth(userId) { _ =>
			checkOwnCart(userId, cartId) {
				withAnyJson { value =>
					val quantity = (value \ "quantity").as[Int]
					cartService.updateQuantity(cartId, quantity) trueOrError
				}
			}
		}
	}
}
