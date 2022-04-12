package services

import dto.{AddressDto, CartDto, OrderDto, ProductDto, UserDto}
import scala.concurrent.Future

//@ImplementedBy(classOf[UserServiceImpl])
trait UserService {
	
	def login(userId: String, userPw: String): Future[_]
	def register(user: UserDto): Future[_]
	def findId(email: String): Future[Option[String]]
	def getUser(userId: String): Future[UserDto]
	def updateQuantity(q: Int)(implicit cartId: Int): Future[Int]
	def addCart(cart: CartDto): Future[Int]
	def newOrder(userId: String, cartIdList: List[Int]): Future[Int]
	def getOrderByUserId(userId: String): Future[List[OrderDto]]
	def addQuantity(implicit cartId: Int): Future[Int]
	def subQuantity(implicit cartId: Int): Future[Int]
	def getCarts(implicit userId: String): Future[List[CartDto]]
	def getCart(implicit cartId: Int): Future[CartDto]
	def deleteCart(implicit cartId: Int): Future[Int]
//	def getAddress(implicit userId: String): Future[Seq[AddressDto]]
	def checkUserOrderedThisProduct(userId: String, productId: Int): Future[Option[ProductDto]]
	
}
