package services.product

import cats.data.OptionT
import dto.ProductDto
import javax.inject.Inject
import models.ProductModel
import scala.concurrent.Future
import slick.jdbc.MySQLProfile.api._
import models.Tables.Products

class SearchServiceImpl @Inject() (productModel: ProductModel)
		extends SearchService {
	
	override def searchProducts(kw: String, code: String): Future[List[ProductDto]] =
		productModel getProducts { product =>
			(product.name like s"%${kw}%") && (product.categoryCode like s"${code}%") }
	
	override def getProductCount(kw: String, code: String): Future[Int] =
		productModel getProductsCount { product =>
			(product.name like s"%${kw}%") && (product.categoryCode like s"${code}%") }
	
	private val orderBy = Vector(
		(p: Products) => p.price.asc,
		(p: Products) => p.price.desc,
		(p: Products) => p.reviewCount.asc
	)
	
	override def searchProductsBy(kw: String, code: String,
						 seq: Int, page: Int, size: Int): Future[List[ProductDto]] =
		productModel.getProductsSortBy(page, size, orderBy(seq)){ p: Products =>
			(p.name like s"%${kw}%") && (p.categoryCode like s"${code}%") }
	
	override def getProductById(productId: Int): OptionT[Future, ProductDto] =
		productModel.getProductById(productId)
}
