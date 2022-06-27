package services.product

import cats.data.OptionT
import dto.ProductDto
import javax.inject.Inject
import models.ProductModel
import scala.concurrent.Future
import slick.jdbc.MySQLProfile.api._

class SearchServiceImpl @Inject()(productModel: ProductModel)
	extends SearchService {
	
	override def searchProducts(kw: String, code: String): Future[List[ProductDto]] =
		productModel getProducts { product =>
			(product.name like s"%${kw}%") && (product.categoryCode like s"${code}%")
		}
	
	override def getProductCount(kw: String, code: String): Future[Int] =
		productModel getProductsCount { product =>
			(product.name like s"%${kw}%") && (product.categoryCode like s"${code}%")
		}
	
	val orderBy = Vector(
		"price asc",
		"price desc",
		"review_count asc",
		"review_count desc"
	)
	
	override def searchProductsOrderBy(keyword: String,
																		 category: String,
																		 sort: Int,
																		 page: Int,
																		 size: Int): Future[List[ProductDto]] =
		productModel.searchProductsOrderBy(keyword, category, page, size, orderBy(sort))
	
	override def getProductById(productId: Int): OptionT[Future, ProductDto] =
		productModel.getProductById(productId)
	
	override def getRandomProducts(size: Int): Future[List[ProductDto]] =
		productModel getRandomProducts size
}
