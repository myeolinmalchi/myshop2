package services.seller

import com.google.inject.ImplementedBy
import dto.{ProductDto, SellerDto, StockResponseDto}
import scala.concurrent.Future

@ImplementedBy(classOf[ProductServiceImpl])
trait ProductService {
	
	def getProductList(sellerId: String): Future[List[ProductDto]]
	def addProduct(product: ProductDto): Future[_]
	def searchProducts(keyword: String): Future[List[ProductDto]]
	def getProductStock(productId: Int): Future[List[StockResponseDto]]
	def updateStock(stockId: Int, adds: Int): Future[Int]
	def checkOwnProduct(sellerId: String, productId: Int): Future[Boolean]
	def checkProductOwnStock(productId: Int, stockId: Int): Future[Boolean]
	
}
