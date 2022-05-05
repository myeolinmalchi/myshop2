package restcontrollers.user

import controllers.Common.CustomFuture
import dto.CartRequestDto
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import restcontrollers.Common._
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import services.NonUserService
import services.user.CartService.{IncorrectProductId, OutOfStock}
import services.user._

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
	import cartService.checkOwnCart
	
	def getCart(userId: String, cartId: Int): Action[AnyContent] = Action.async { implicit request =>
		withUserAuth(userId) { _ =>
			checkOwnCart(userId, cartId) {
				cartService.getCart(cartId) map {
					case Some(cart) => Ok(Json.toJson(cart))
					case None => NotFound
				}
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
			withJson[CartRequestDto] { implicit cart =>
				cartService.addCart map {
					case Right(_) => Created // 201
					case Left(IncorrectProductId) => Conflict // 409
					case Left(failure: OutOfStock) =>
						Forbidden(Json.toJson(Map("stock" -> failure.stock))) // 403
				} recover {
					case _: NoSuchElementException => Conflict // 409
					case ex: Exception =>
						BadRequest(Json.toJson(Map("error" -> ex.getMessage))) // 400
				}
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
