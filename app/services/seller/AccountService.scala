package services.seller

import com.google.inject.ImplementedBy
import dto.SellerDto
import scala.concurrent.Future

@ImplementedBy(classOf[AccountServiceImpl])
trait AccountService {
	
	def login(implicit seller: SellerDto): Future[_]
	def register(implicit seller: SellerDto): Future[_]
	def findId(email: String): Future[Option[String]]
	def getSellerOption(sellerId: String): Future[Option[SellerDto]]
	
}
