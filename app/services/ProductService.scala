package services

import dto._
import models.{CategoryModel, ProductModel, ReviewModel}
import models.Tables.Products
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.MySQLProfile.api._
import javax.inject._

@Singleton
class ProductService @Inject() (productModel: ProductModel,
								categoryModel: CategoryModel,
								reviewModel: ReviewModel)
							   (implicit ec: ExecutionContext) {
	
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
		
	def getReviewsByProductId(productId: Int): Future[List[ReviewDto]] =
		reviewModel getReviewsByProductId productId
	
	def getProductStock(productId: Int): Future[List[StockResponseDto]] =
		productModel getProductStock(productId)
		
	def getSellerId(productId: Int): Future[String] = ???
	
	def getProductOptionStock(productId: Int, depth: Int, parentId: Int): Future[List[StockResponseDto]] =
		productModel getProductOptionStock(productId, depth, parentId)
	
	def getMainCategories: Future[List[(String, String)]] =
		categoryModel.getMainCategories
	
	def getChildrenCategories(code: String): Future[List[(String, String)]] =
		categoryModel.getChildrens(code)
	
	def getSiblingCategories(code: String): Future[List[(String, String)]] =
		categoryModel.getSiblings(code)
	
}
