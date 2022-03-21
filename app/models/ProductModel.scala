package models

import scala.concurrent.ExecutionContext
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
import play.api.libs.json.Json
import scala.language.postfixOps
import slick.lifted.AbstractTable

@Singleton
class ProductModel(db: Database)(implicit ec: ExecutionContext) {
	implicit val products = Products
	implicit val options = ProductOptions
	implicit val items = ProductOptionItems
	
	val commonApi = new CommonModelApi(db)
	import commonApi._

	def getProductList(f: Products => Rep[Boolean]): Future[List[ProductDto]] ={
		def getProducts =
			select[Products, ProductDto](ProductDto.newInstance) { product => f(product) }

		def getOptions(implicit product: ProductDto) =
			select[ProductOptions, ProductOptionDto](ProductOptionDto.newInstance) { option =>
				option.productId === product.productId }

		def getItems(implicit option: ProductOptionDto) =
			select[ProductOptionItems, ProductOptionItemDto](ProductOptionItemDto.newInstance) { item =>
				item.productOptionId === option.productOptionId }
		
		getProducts map(_ map { implicit product => for {
			optionList <- getOptions map (_ map { implicit option =>
				for { itemList <- getItems } yield option setItems itemList })
			options <- Future sequence optionList
		} yield product setOptions options }) map (Future sequence _) flatten
	}
	
	def addProduct(implicit sellerId: String, p: ProductDto): Future[_] = {
		def insertProduct(implicit p: ProductDto): Future[Int] = p.optionList match {
			case Nil =>
				Future.failed(new NoSuchElementException(s"[${p.name}] 메인옵션은 필수항목입니다."))
			case _ => {
				println("option exists")
				val query = (Products returning Products.map(_.productId))
				val row = ProductsRow(0, p.name, p.sellerId, p.price, p.categoryCode, p.detailInfo, p.thumbnail, 0, 0)
				db run (query += row).asTry map {
					case Failure(e) => throw e
					case Success(rs) => rs
				}
			}
		}
		
		def insertOption(o: ProductOptionDto, productId: Future[Int]): Future[Int] = productId transform {
			case Success(id: Int) =>
				o.itemList match {
					case Nil => Failure(new Exception("error"))
					case _ =>
						val query = (ProductOptions returning ProductOptions.map(_.productOptionId))
						val row = ProductOptionsRow(id, 0, o.name, o.optionSequence, o.images)
						Try(db.run(query += row))
				}
			case Failure(e) => Try(Future.failed(e))
		} flatMap (identity)
		
		def insertItem(i: ProductOptionItemDto, optionId: Future[Int]): Future[Int] = optionId transform {
			case Success(id: Int) =>
				val row = ProductOptionItemsRow(id, -1, i.name, i.itemSequence, i.surcharge, i.stock)
				Try(db.run(ProductOptionItems += row))
			case Failure(e) => Try(Future.failed(e))
		} flatMap(identity)
		
		val productId = insertProduct
		Future.sequence(p.optionList flatMap { option =>
			val optionId = insertOption(option, productId)
			option.itemList map { item =>
				insertItem(item, optionId)
			}
		})
	}
}
