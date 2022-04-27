package services.product

import com.google.inject.ImplementedBy
import dto.StockResponseDto
import scala.concurrent.Future

@ImplementedBy(classOf[StockServiceImpl])
trait StockService {
	
	def getProductStock(productId: Int): Future[List[StockResponseDto]]
	def getProductOptionStock(productId: Int, depth: Int, parentId: Int): Future[List[StockResponseDto]]
	
}
