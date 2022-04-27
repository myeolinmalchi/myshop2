package services.product

import javax.inject.Inject
import models.CategoryModel
import scala.concurrent.Future

class CategoryServiceImpl @Inject() (categoryModel: CategoryModel)
		extends CategoryService {
	
	override def getMainCategories: Future[List[(String, String)]] =
		categoryModel.getMainCategories
	
	override def getChildrenCategories(code: String): Future[List[(String, String)]] =
		categoryModel.getChildrens(code)
	
	override def getSiblingCategories(code: String): Future[List[(String, String)]] =
		categoryModel.getSiblings(code)
	
}
