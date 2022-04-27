package services.user

import com.google.inject.ImplementedBy
import dto.OrderDto
import scala.concurrent.Future

@ImplementedBy(classOf[OrderServiceImpl])
trait OrderService {
	
	def newOrder(userId: String, cartIdList: List[Int]): Future[Int]
	def getOrderByUserId(userId: String): Future[List[OrderDto]]
	def checkUserOrderedThisProduct(userId: String, productId: Int): Future[_]
	
}
