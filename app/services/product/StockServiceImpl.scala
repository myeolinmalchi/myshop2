package services.product

import dto.StockResponseDto
import javax.inject.Inject
import models.ProductModel
import scala.concurrent.Future

class StockServiceImpl @Inject() (productModel: ProductModel)
		extends StockService {
	
	override def getProductStock(productId: Int): Future[List[StockResponseDto]] =
		productModel getProductStock(productId)
	
	override def getProductOptionStock(productId: Int, depth: Int, parentId: Int): Future[List[StockResponseDto]] =
		productModel getProductOptionStock(productId, depth, parentId)
	
}
