package services.user

import com.google.inject.ImplementedBy
import dto.{CartDto, UserDto}
import scala.concurrent.Future

@ImplementedBy(classOf[CartServiceImpl])
trait CartService{
	
	def addCart(implicit cart: CartDto): Future[Int]
	def updateQuantity(q: Int)(implicit cartId: Int): Future[Int]
	def addQuantity(implicit cartDto: CartDto): Future[Int]
	def subQuantity(implicit cartDto: CartDto): Future[Int]
	def getCarts(implicit user: UserDto): Future[List[CartDto]]
	def getCart(implicit cartDto: CartDto): Future[CartDto]
	def deleteCart(implicit cartDto: CartDto): Future[Int]
	
	def updateQuantity2(cartId: Int, quantity: Int): Future[Int]
	def addQuantity(cartId: Int): Future[Int]
	def subQuantity(cartId: Int): Future[Int]
	def getCarts(userId: String): Future[List[CartDto]]
	def getCart(cartId: Int): Future[CartDto]
	def deleteCart(cartId: Int): Future[Int]
	
	def checkOwnCart(userId: String, cartId: Int): Future[Boolean]
	
}
