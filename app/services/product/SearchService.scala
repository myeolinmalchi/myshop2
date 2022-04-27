package services.product

import com.google.inject.ImplementedBy
import dto.ProductDto
import scala.concurrent.Future

@ImplementedBy(classOf[SearchServiceImpl])
trait SearchService {
	
	def searchProducts(kw: String, code: String): Future[List[ProductDto]]
	def getProductCount(kw: String, code: String): Future[Int]
	def searchProductsBy(kw: String, code: String,
						 seq: Int, page: Int, size: Int): Future[List[ProductDto]]
	def getProductById(productId: Int): Future[ProductDto]
	
}
