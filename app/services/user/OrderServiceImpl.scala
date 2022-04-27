package services.user

import dto.OrderDto
import javax.inject.{Inject, Singleton}
import models.{CartModel, OrderModel}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderServiceImpl @Inject() (cartModel: CartModel,
								  orderModel: OrderModel)
								(implicit ec: ExecutionContext) extends OrderService {
	
	def newOrder(userId: String, cartIdList: List[Int]): Future[Int] =
		cartModel newOrder(userId, cartIdList)
	
	def getOrderByUserId(userId: String): Future[List[OrderDto]] =
		orderModel getOrdersByUserId(userId)
	
	def checkUserOrderedThisProduct(userId: String, productId: Int): Future[_] =
		orderModel checkUserOrderedThisProduct (userId, productId)
	
}
