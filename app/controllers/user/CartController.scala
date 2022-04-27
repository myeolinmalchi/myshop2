package controllers.user

import common.json.CustomJsonApi._
import controllers.Common._
import controllers.user.CommonApi._
import dto.CartDto
import javax.inject.{Inject, Singleton}
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
	
	implicit class CartFuture(c: Future[List[CartDto]])
								  (implicit request: Request[AnyContent]){
		def cartOrError: Future[Result] = c map { result =>
			Ok(views.html.cart_list(result))
		} recover {
			case ex: Exception => ex toJsonError
		}
	}
	
	def cartList: Action[AnyContent] = Action.async { implicit request =>
		withUser { implicit user =>
			cartService.getCarts cartOrError
		} ifNot withNonUserToken { token =>
			nonUserService.getCarts(token) cartOrError
		}
	}
	
	def addCart(): Action[AnyContent] = Action.async { implicit request =>
		withJsonDto[CartDto] { implicit cart =>
			withUser { _ =>
				cartService.addCart trueOrError
			} ifNot withNonUserToken { _ =>
				nonUserService.addCart(cart) trueOrError
			}
		}
	}
	
	def deleteCart(): Action[AnyContent] = Action.async { implicit request =>
		withAnyJson { value =>
			val cartId = (value \ "cartId").as[Int]
			withUser { _ =>
				userService.deleteCart(cartId) trueOrError
			} ifNot withNonUserToken { _ =>
				nonUserService.deleteCart(cartId) trueOrError
			}
		}
	}
	
	def deleteCartTest(): Action[AnyContent] = Action.async { implicit request =>
		withJsonDto[CartDto] { implicit cart =>
			withUser { _ =>
				cartService.deleteCart trueOrError
			} ifNot withNonUserToken { _ =>
				nonUserService.deleteCart(cart.cartId) trueOrError
			}
		}
	}
	
	def updateCartQuantity(): Action[AnyContent] = Action.async { implicit request =>
		withAnyJson { value =>
			val quantity = (value \ "quantity").as[Int]
			val cartId = (value \ "cartId").as[Int]
			withUser { _ =>
				userService.updateQuantity(quantity)(cartId) trueOrError
			} ifNot withNonUserToken { _ =>
				nonUserService.updateQuantity(quantity)(cartId) trueOrError
			}
		}
	}
}
