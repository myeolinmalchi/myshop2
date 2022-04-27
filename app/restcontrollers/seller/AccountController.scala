package restcontrollers.seller

import dto.SellerDto
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import restcontrollers.Common.{CustomResult, ROLE_SELLER, withJson}
import scala.concurrent.ExecutionContext
import services.seller.AccountService

class AccountController @Inject()(cc: ControllerComponents)
								 (implicit ec: ExecutionContext,
								  accountService: AccountService)
		extends AbstractController(cc) {
	
	def register: Action[AnyContent] = Action.async { implicit request =>
		withJson[SellerDto]{ implicit seller =>
			accountService.register map(_ => Ok) recover {
				case ex: Exception => BadRequest(Json.toJson(Map("error" -> ex.getMessage)))
			}
		}
	}
	
	def login: Action[AnyContent] = Action.async { implicit request =>
		withJson[SellerDto]{ implicit seller =>
			accountService.login map { _ =>
				Ok.withJwtHeader(seller.sellerId, ROLE_SELLER)
			} recover {
				case ex: Exception => Unauthorized(Json.toJson(Map("error"-> ex.getMessage)))
			}
		}
	}
	
}