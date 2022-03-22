package services

import dto._
import models.{CategoryModel, ProductModel}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.MySQLProfile.api._

class ProductService(db: Database)(implicit ec: ExecutionContext) {
	
	val productModel = new ProductModel(db)
	val categoryModel = new CategoryModel(db)
	
	def searchProducts(kw: String, code: String): Future[List[ProductDto]] =
		productModel getProductList { product =>
			(product.name like s"%${kw}%") && (product.categoryCode like s"${code}%") }
	
	def getMainCategories =
		categoryModel.getMainCategories
	
	def getChildrenCategories(code: String) =
		categoryModel.getChildrens(code)
	
	def getSiblingCategories(code: String) =
		categoryModel.getSiblings(code)
}
