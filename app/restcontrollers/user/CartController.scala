package restcontrollers.user

import common.validation.CartValidationLib
import common.validation.CartValidationLib._
import common.validation.ValidationResultLib.ValidationFailure
import controllers.Common.CustomFuture
import dto.CartRequestDto
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import restcontrollers.Common._
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import services.NonUserService
import services.user._

@Singleton
class CartController @Inject()(cc: ControllerComponents)
															(implicit ec: ExecutionContext,
															 accountService: AccountService,
															 cartService: CartService,
															 orderService: OrderService,
															 authService: AuthService,
															 nonUserService: NonUserService,
															 nonUserCartService: services.nonuser.CartService)
	extends AbstractController(cc) with CartValidationLib{
	
	import authService._
	import cartService.checkOwnCart
	
	def getCart(userId: String, cartId: Int): Action[AnyContent] = Action.async { implicit request =>
		withUser(userId) { _ =>
			checkOwnCart(userId, cartId) {
				cartService.getCart(cartId) map {
					case Some(cart) => Ok(Json.toJson(cart))
					case None => NotFound
				}
			}
		}
	}
	
	def getCarts(userId: String): Action[AnyContent] = Action.async { implicit request =>
		withUser(userId) { implicit user =>
			cartService.getCarts(userId) getOrError
		}
	}
	
	def addCart(userId: String): Action[AnyContent] = Action.async { implicit request =>
		withJson[CartRequestDto] { cart =>
			withUserAndValidation(userId) (
				authSuccess = cartService addCart cart,
				authFailure = nonUserCartService addCart cart.setUserId(_),
				result = { validationResult: Either[ValidationFailure, Int] =>
					validationResult match {
						case Right(_) => Created // 201
						case Left(IncorrectProductId) => Conflict // 409
						case Left(OutOfStock(stock)) =>
							Forbidden(Json.toJson(Map("stock" -> stock))) // 403
						case Left(IncorrectItemSize(size)) => // 400
							BadRequest(Json.obj(
								"error" ->
									s"옵션 항목의 개수가 일치하지 않습니다.(옵션 수: ${size}개)"
							))
						case Left(ProductNotExists) => NotFound
					}
				}
			)
		}
	}
	
	def deleteCart(userId: String, cartId: Int): Action[AnyContent] = Action.async { implicit request =>
		withUser(userId) { _ =>
			checkOwnCart(userId, cartId) {
				cartService.deleteCart(cartId) trueOrError
			}
		}
	}
	
	def updateQuantity(userId: String, cartId: Int): Action[AnyContent] = Action.async { implicit request =>
		withUser(userId) { _ =>
			checkOwnCart(userId, cartId) {
				withAnyJson { value =>
					val quantity = (value \ "quantity").as[Int]
					cartService.updateQuantity(cartId, quantity) trueOrError
				}
			}
		}
	}
}
