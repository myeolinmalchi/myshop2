package controllers.seller

import dto.SellerDto
import java.time.LocalDateTime
import models.SellerSessionModel
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{AnyContent, Call, Flash, Request, RequestHeader, Result}
import scala.concurrent.{ExecutionContext, Future}
import services.SellerService

object CommonApi {
	
	implicit val defaultPage: Call = controllers.seller.routes.IndexController.index
	
	def extractSeller(req: RequestHeader)
					 (implicit ec: ExecutionContext,
					  sellerService: SellerService): Future[Option[SellerDto]] = {
		val sessionTokenOpt = req.session.get("sessionToken")
		def swap[M](x: Option[Future[M]]): Future[Option[M]] =
			Future.sequence(Option.option2Iterable(x)).map(_.headOption)
		swap (sessionTokenOpt
				.flatMap(token => SellerSessionModel.getSession(token))
				.filter(_.expiration.isAfter(LocalDateTime.now()))
				.map(_.sellerId)
				.map(sellerService.getSeller))
	}
	
	def withSeller(block: SellerDto => Future[Result])
				  (implicit request: Request[AnyContent], flash: Flash,
				   ec: ExecutionContext, sellerService: SellerService): Future[Result] =
		extractSeller(request) flatMap {
			case Some(seller) => block(seller)
			case None => Future(Unauthorized(views.html.seller.no_auth()))
		}
	
	def withoutSeller(block: => Future[Result])
						(implicit request: Request[AnyContent], flash: Flash,
						 ec: ExecutionContext, sellerService: SellerService): Future[Result] =
		extractSeller(request) flatMap {
			case Some(_) => Future(Unauthorized(views.html.seller.index()))
			case None => block
		}
	
}
