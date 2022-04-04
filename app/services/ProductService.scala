package services

import dto._
import models.{CategoryModel, ProductModel}
import models.Tables.Products
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.MySQLProfile.api._
import javax.inject._

@Singleton
class ProductService (db: Database)(implicit ec: ExecutionContext) {
	
	val productModel = new ProductModel(db)
	val categoryModel = new CategoryModel(db)
	
	def searchProducts(kw: String, code: String): Future[List[ProductDto]] =
		productModel getProducts { product =>
			(product.name like s"%${kw}%") && (product.categoryCode like s"${code}%") }
			
	def getProductCount(kw: String, code: String): Future[Int] =
		productModel getProductsCount { product =>
			(product.name like s"%${kw}%") && (product.categoryCode like s"${code}%") }
	
	private val orderBy = Vector(
		(p: Products) => p.price.asc,
		(p: Products) => p.price.desc,
		(p: Products) => p.reviewCount.asc
	)
	
	def searchProductsBy(kw: String, code: String,
						 seq: Int, page: Int, size: Int): Future[List[ProductDto]] =
		productModel.getProductsSortBy(page, size, orderBy(seq)){ p: Products =>
			(p.name like s"%${kw}%") && (p.categoryCode like s"${code}%") }
			
	def getProductById(productId: Int): Future[ProductDto] =
		productModel.getProductById(productId)
	
	def getProductStock(productId: Int): Future[List[StockResponseDto]] =
		productModel getProductStock(productId)
		
	def getSellerId(productId: Int): Future[String] = ???
	
		
	def getProductOptionStock(productId: Int, depth: Int, parentId: Int): Future[List[StockResponseDto]] =
		productModel getProductOptionStock(productId, depth, parentId)
	
	def getMainCategories =
		categoryModel.getMainCategories
	
	def getChildrenCategories(code: String) =
		categoryModel.getChildrens(code)
	
	def getSiblingCategories(code: String) =
		categoryModel.getSiblings(code)
}
