package services.seller

import dto.{ProductDto, SellerDto, StockResponseDto}
import javax.inject.{Inject, Singleton}
import models.{ProductModel, SellerModel}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api._

@Singleton
class ProductServiceImpl @Inject()(productModel: ProductModel,
								   sellerModel: SellerModel)
								  (implicit ec: ExecutionContext)
	extends ProductService{
	
	override def getProductList(sellerId: String): Future[List[ProductDto]] =
		productModel.getProducts(product => product.sellerId === sellerId)
	
	override def addProduct(product: ProductDto): Future[_] =
		productModel.insertProductWithAll(product)
	
	override def searchProducts(keyword: String): Future[List[ProductDto]] =
		productModel getProducts { product => product.name like s"%${keyword}%" }
	
	override def getProductStock(productId: Int): Future[List[StockResponseDto]] =
		productModel getProductStock(productId)
	
	override def updateStock(stockId: Int, adds: Int): Future[Int] =
		productModel updateStock(stockId, adds)
	
	override def checkOwnProduct(sellerId: String, productId: Int): Future[Boolean] =
		productModel getSellerByProductId productId map {
			case Some(id) => id == sellerId
			case None => false
		}
		
	override def checkProductOwnStock(productId: Int, stockId: Int): Future[Boolean] =
		productModel getStockProductId stockId map {
			case Some(id) => id == productId
			case None => false
		}
}
