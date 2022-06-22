package services

import cats.data.OptionT
import dto._
import javax.inject._
import models.{CategoryModel, ProductModel, ReviewModel}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.MySQLProfile.api._

@Singleton
class ProductService @Inject()(productModel: ProductModel,
															 categoryModel: CategoryModel,
															 reviewModel: ReviewModel)
															(implicit ec: ExecutionContext) {
	
	def searchProducts(kw: String, code: String): Future[List[ProductDto]] =
		productModel getProducts { product =>
			(product.name like s"%${kw}%") && (product.categoryCode like s"${code}%")
		}
	
	def getProductCount(kw: String, code: String): Future[Int] =
		productModel getProductsCount { product =>
			(product.name like s"%${kw}%") && (product.categoryCode like s"${code}%")
		}
	
	val orderBy = Vector(
		"price asc",
		"price desc",
		"review_count asc",
		"review_count desc"
	)
	
	def searchProductsBy(kw: String, code: String,
											 seq: Int, page: Int, size: Int): Future[List[ProductDto]] =
		productModel.searchProductsOrderBy(kw, code, page, size, orderBy(seq))
	
	def getProductById(productId: Int): OptionT[Future, ProductDto] =
		productModel getProductById productId
	
	def getReviewsByProductId(productId: Int): Future[List[ReviewResponseDto]] =
		reviewModel getReviewsByProductId productId
	
	def getProductStock(productId: Int): Future[List[StockResponseDto]] =
		productModel getProductStock (productId)
	
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
