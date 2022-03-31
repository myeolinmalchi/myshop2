package controllers

import cats.data.OptionT
import dto._
import java.time.LocalDateTime
import javax.inject._
import models.{ProductModel, SellerSessionModel}
import play.api.db.slick._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import services.{ProductService, SellerService}
import slick.jdbc.JdbcProfile

@Singleton
class SellerController @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
								cc: ControllerComponents)(implicit ec: ExecutionContext)
		extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {
	private val service = new SellerService(db)
	private val productService = new ProductService(db)
	
	implicit val sellerReads = Json.reads[SellerDto]
	implicit val itemReads = Json.reads[ProductOptionItemDto]
	implicit val imageRead = Json.reads[ProductImageDto]
	implicit val optionReads = Json.reads[ProductOptionDto]
	implicit val productReads = Json.reads[ProductDto]
	implicit val stockReads = Json.reads[StockDto]
	
	implicit val itemWrites = Json.writes[ProductOptionItemDto]
	implicit val imageWrite = Json.writes[ProductImageDto]
	implicit val optionWrites = Json.writes[ProductOptionDto]
	implicit val productWrites = Json.writes[ProductDto]
	implicit val stockWrites = Json.writes[StockDto]
	implicit val stockTestWrites = Json.writes[StockResponseDto]
	
	private def withJsonBody[A](f: A => Future[Result])(implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
		request.body.asJson.map { body =>
			Json.fromJson[A](body) match {
				case JsSuccess(a, path) => f(a)
				case e @JsError(_) => {
					println(e)
					Future.successful(Redirect(routes.SellerController.index))
				}
			}
		}.getOrElse((Future.successful(Redirect(routes.SellerController.index))))
	}
	
	private def extractSeller(req: RequestHeader): Future[Option[SellerDto]] = {
		val sessionTokenOpt = req.session.get("sessionToken")
		def swap[M](x: Option[Future[M]]): Future[Option[M]] =
			Future.sequence(Option.option2Iterable(x)).map(_.headOption)
		swap (sessionTokenOpt
				.flatMap(token => SellerSessionModel.getSession(token))
				.filter(_.expiration.isAfter(LocalDateTime.now()))
				.map(_.sellerId)
				.map(service.getSeller))
	}
	
	private def withSeller[T](block: SellerDto => Future[Result])
							 (implicit request: Request[AnyContent]): Future[Result] =
		extractSeller(request) flatMap {
			case Some(seller) => block(seller)
			case None => Future(Unauthorized(views.html.seller.no_auth()))
		}
	
	private def withoutSeller[T](block: => Future[Result])
							 (implicit request: Request[AnyContent]): Future[Result] =
		extractSeller(request) flatMap {
			case Some(_) => Future(Unauthorized(views.html.seller.index()))
			case None => block
		}
	
	def index = Action { implicit request =>
		Ok(views.html.seller.index())
	}
	
	def loginPage = Action.async { implicit request =>
		withoutSeller[SellerDto] {
			Future(Ok(views.html.seller.login()))
		}
	}
	
	def login = Action.async { implicit request =>
		withJsonBody[SellerDto] { seller =>
			service.login(seller.sellerId, seller.sellerPw).map {
				case Some(able) =>
					if(able) {
						val token = SellerSessionModel.generateToken(seller.sellerId, request.session)
						Ok(Json.toJson(true)).withSession("sessionToken" -> token)
					}
					else Ok(Json.toJson(Map("error"->"비밀번호가 일치하지 않습니다.")))
				case None => Ok(Json.toJson(Map("error" -> "존재하지 않는 계정입니다.")))
			}
		}
	}
	
	def registerPage = Action.async { implicit request =>
		withoutSeller[SellerDto]{
			Future(Ok(views.html.seller.register()))
		}
	}
	
	def register = Action async { implicit request =>
		withoutSeller[SellerDto] {
			withJsonBody[SellerDto] { seller =>
				service.register(seller).map {
					case Some(e) => Ok(Json.toJson(Map("error" -> e)))
					case None => Ok(Json.toJson(true))
				}
			}
		}
	}
	
	def logout = Action.async { implicit request =>
		withSeller[SellerDto]  { seller =>
			SellerSessionModel.remSession(request.session)
			Future(Redirect(routes.SellerController.index).withSession(request.session - "sessionToken"))
		}
	}
	
	def findSellerPage = Action { Ok(views.html.findUser()) }
	
	def findId = Action.async { implicit request =>
		withJsonBody[SellerDto] { seller =>
			service.findId(seller.email).map {
				case Some(sellerId) => Ok(Json.toJson(Map("sellerId" -> sellerId)))
				case None => Ok(Json.toJson(false))
			}
		}
	}
	
	def productRegisterPage = Action.async { implicit request =>
		withSeller[SellerDto] { seller =>
			productService.getMainCategories flatMap { categories =>
				Future(Ok(views.html.seller.product.register(categories, seller.sellerId)))
			}
		}
	}
	
	def addProduct = Action.async { implicit request =>
		withSeller[SellerDto] { seller =>
			withJsonBody[ProductDto] { product =>
				service.addProduct(seller.sellerId, product) transform {
					case Success(_) => Try(Ok(Json.toJson(true)))
					case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
				}
			}
		}
	}
	
	def getProducts = Action.async { implicit request =>
		withSeller[SellerDto] { seller =>
			val st = System.nanoTime()
			service.getProductList(seller.sellerId) transform {
				case Success(products) => {
					val ed = System.nanoTime()
					val time = (ed-st)/1000000
					println(s"상품 수: ${products.size} , 걸린 시간 ${time}ms")
					Try(Ok(views.html.seller.product.product_list(products)))
				}
				case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
			}
		}
	}
	
	def getProductStock = Action.async { implicit request =>
		withSeller[SellerDto] { seller =>
			withJsonBody[StockDto] { stock =>
				service.getProductStock(stock.productId) transform {
					case Success(result) => Try(Ok(Json.toJson(result)))
					case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
				}
			}
		}
	}
	
	def updateStock = Action.async { implicit request =>
		withSeller[SellerDto] { seller =>
			withJsonBody[StockDto]	{ stock =>
				service.updateStock(stock.productStockId, stock.stock) transform {
					case Success(result) => Try(Ok(Json.toJson(result)))
					case Failure(e) => Try(Ok(Json.toJson(Map("error" -> e.getMessage))))
				}
			}
		}
	}
	
	
}