package services

import scala.concurrent.ExecutionContext
import common.encryption.SHA256.encrypt
import dto._
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api._
import models.Tables._
import scala.collection.mutable.Map
import scala.util.{Failure, Success, Try}
import java.security.MessageDigest
import java.math.BigInteger
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import common.encryption._
import models.{CommonModelApi, ProductModel, SellerModel}
import play.api.libs.json.Json
import scala.language.postfixOps
import slick.lifted.AbstractTable

class ProductService(db: Database)(implicit ec: ExecutionContext) {
	
	val productModel = new ProductModel(db)
	
	def searchProducts(kw: String, code: String): Future[List[ProductDto]] = {
		productModel getProductList { product =>
			(product.name like s"%${kw}%") && (product.categoryCode like s"${code}%") }
	}
	
}
