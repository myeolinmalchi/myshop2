package models

import cats.implicits._
import dto._
import javax.inject._
import models.Tables._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import slick.jdbc.MySQLProfile.api._

@Singleton
class ProductModel(db: Database)(implicit ec: ExecutionContext) {
	implicit val products = Products
	implicit val options = ProductOptions
	implicit val items = ProductOptionItems
	
	val commonApi = new CommonModelApi(db)
	import commonApi._
	
	def getProducts(g: ProductDto => Future[ProductDto])
				   (implicit f: Products => Rep[Boolean]): Future[List[ProductDto]] =
		select[Products, ProductDto](ProductDto.newInstance) { product =>
			f(product) } flatMap(_ traverse(product => g(product)))
	
	def getOptions(g: ProductOptionDto => Future[ProductOptionDto])
				  (implicit product: ProductDto): Future[ProductDto] =
		select[ProductOptions, ProductOptionDto](ProductOptionDto.newInstance) { option =>
			option.productId === product.productId } flatMap(_ traverse(option => g(option))) map(option =>
				product.setOptions(option))
	
	def getItems(implicit option: ProductOptionDto): Future[ProductOptionDto] =
		select[ProductOptionItems, ProductOptionItemDto](ProductOptionItemDto.newInstance) { item =>
			item.productOptionId === option.productOptionId } map (item => option.setItems(item))
		
	def getProductsWithAll(implicit f: Products => Rep[Boolean]): Future[List[ProductDto]] =
		getProducts { implicit product => getOptions { implicit option => getItems } }
	
	def insertProduct(p: ProductDto)(f: ProductDto => Future[ProductDto]): Future[ProductDto] = p.optionList match {
		case Nil =>
			Future.failed(new NoSuchElementException(s"[${p.name}] 메인옵션은 필수항목입니다."))
		case _ => {
			val query = Products returning Products.map(_.productId)
			val row = ProductsRow(0, p.name, p.sellerId, p.price, p.categoryCode, p.detailInfo, p.thumbnail, 0, 0)
			db run (query+=row) map(id => p.setProductId(id)) flatMap(f(_))
		}
	}
	
	def insertImage(i: ProductImageDto): Future[ProductImageDto] = {
		val row = ProductImagesRow(i.productId, -1, i.image, i.sequence)
		db run (ProductImages+=row)
		Future(i)
	}
	
	def insertOption(o: ProductOptionDto): Future[ProductOptionDto] = o.itemList match {
		 case Nil => Future.failed(new Exception("error"))
		 case _ =>
			 val query = (ProductOptions returning ProductOptions.map(_.productOptionId))
			 val row = ProductOptionsRow(o.productId, 0, o.name, o.optionSequence)
			 for {
				 optionId <- db.run(query += row)
			 } yield o.setOptionId(optionId)
	}
	
	def insertItem(i: ProductOptionItemDto): Future[ProductOptionItemDto] = {
		val row = ProductOptionItemsRow(i.productOptionId, -1, i.name, i.itemSequence, i.surcharge, i.stock)
		db.run(ProductOptionItems += row)
		Future(i)
	}
	
	def insertProductWithAll(p: ProductDto): Future[ProductDto] =
		insertProduct(p) { p =>
			p.optionList traverse { o =>
				insertOption(o.setProductId(p.productId))
			} flatMap ( _ traverse { o =>
				o.itemList traverse { i =>
					insertItem(i.setOptionId(o.productOptionId))
				} map o.setItems
			}) map p.setOptions
		} flatMap { p =>
			p.imageList traverse { i =>
				insertImage(i.setProductId(p.productId))
			} map p.setImages
		}
	// BFS로 재고 정보 저장
	def insertProductStock(p: ProductDto): Future[_] = {
		def go(os: List[ProductOptionDto], pid: Int, depth: Int): Future[_] = os match {
			case h :: t => h.itemList traverse { item =>
				println(item.name)
				val query = (ProductStock returning ProductStock.map(_.productStockId))
				val row = ProductStockRow(0, pid, item.name, depth)
				db run (query += row) map (go(t, _, depth + 1))}
			case Nil => Future(pid)
		}
		go(p.optionList, 0, 0)
	}
}
