package controllers.seller

import common.json.CustomJsonApi._
import controllers.routes
import controllers.seller.CommonApi._
import dto.SellerDto
import javax.inject.Inject
import models.SellerSessionModel
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import services.SellerService

class AccountController @Inject()(cc: ControllerComponents)
								 (implicit ec: ExecutionContext,
									   sellerService: SellerService)
		extends AbstractController(cc) {
	
	def loginPage: Action[AnyContent]= Action.async { implicit request =>
		withoutSeller { Future(Ok(views.html.seller.login())) }
	}
	
	def login: Action[AnyContent]= Action.async { implicit request =>
		withJsonDto[SellerDto] { seller =>
			sellerService.login(seller.sellerId, seller.sellerPw).map {
				case Some(able) if able =>
					val token = SellerSessionModel.generateToken(seller.sellerId, request.session)
					Ok(Json.toJson(true)).withSession("sessionToken" -> token)
				case None => Ok(Json.toJson(Map("error" -> "존재하지 않는 계정입니다.")))
				case _ => Ok(Json.toJson(Map("error"->"비밀번호가 일치하지 않습니다.")))
			}
		}
	}
	
	def registerPage: Action[AnyContent]= Action.async { implicit request =>
		withoutSeller{ Future(Ok(views.html.seller.register())) }
	}
	
	def register: Action[AnyContent]= Action async { implicit request =>
		withoutSeller{
			withJsonDto[SellerDto] { seller =>
				sellerService.register(seller).map {
					case Some(e) => Ok(Json.toJson(Map("error" -> e)))
					case None => Ok(Json.toJson(true))
				}
			}
		}
	}
	
	def logout: Action[AnyContent]= Action.async { implicit request =>
		withSeller { seller =>
			SellerSessionModel.remSession(request.session)
			Future(Redirect(defaultPage)
					.withSession(request.session - "sessionToken"))
		}
	}
}
