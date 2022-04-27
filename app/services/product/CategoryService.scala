package services.product

import com.google.inject.ImplementedBy
import scala.concurrent.Future

@ImplementedBy(classOf[CategoryServiceImpl])
trait CategoryService {
	
	def getMainCategories: Future[List[(String, String)]]
	def getChildrenCategories(code: String): Future[List[(String, String)]]
	def getSiblingCategories(code: String): Future[List[(String, String)]]
	
}
