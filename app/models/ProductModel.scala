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
	implicit val images = ProductImages
	
	private val commonApi = new CommonModelApi(db)
	import commonApi._
	
	private object InnerApi {
		def getProducts(g: ProductDto => Future[ProductDto])
					   (implicit f: Products => Rep[Boolean]) =
			select[Products, ProductDto](ProductDto.newInstance) { product =>
				f(product) } flatMap(_ traverse(product => g(product)))
				
		def getProduct(productId: Int, g: ProductDto => Future[ProductDto]) =
			selectOne[Products, ProductDto](ProductDto.newInstance) { product =>
				product.productId === productId
			} flatMap(_ map(product => g(product)) getOrElse(throw new Exception()))
		
		def getProductsSortBy(page: Int, n: Int, g: ProductDto => Future[ProductDto],
							  h: Products => slick.lifted.ColumnOrdered[_])
							 (implicit f: Products => Rep[Boolean]) =
			db.run(Products.filter(product => f(product)).sortBy(h).drop(n*(page-1)).take(n).result)
					.map(_ map{ row => ProductDto.newInstance(row)} toList)
					.flatMap(_ traverse(product => g(product)))
		
		def getImages(product: ProductDto): Future[ProductDto] =
			select[ProductImages, ProductImageDto](ProductImageDto.newInstance) { image =>
				image.productId === product.productId } map (image => product.setImages(image))
		
		def getOptions(product: ProductDto, g: ProductOptionDto => Future[ProductOptionDto]) =
			select[ProductOptions, ProductOptionDto](ProductOptionDto.newInstance) { option =>
				option.productId === product.productId } flatMap(_ traverse(option => g(option))) map(option =>
				product.setOptions(option))
		
		def getItems(option: ProductOptionDto) =
			select[ProductOptionItems, ProductOptionItemDto](ProductOptionItemDto.newInstance) { item =>
				item.productOptionId === option.productOptionId } map (item => option.setItems(item))
		
		def insertProduct(p: ProductDto)(f: ProductDto => Future[ProductDto]) = p.optionList match {
			case Nil => Future.failed(new NoSuchElementException(s"[${p.name}] 메인옵션은 필수항목입니다."))
			case _ => {
				val query = Products returning Products.map(_.productId)
				val row = ProductsRow(0, p.name, p.sellerId, p.price, p.categoryCode, p.detailInfo, p.thumbnail, 0, 0)
				db run (query+=row) map(id => p.setProductId(id)) flatMap(f(_))
			}
		}
		
		def insertImage(i: ProductImageDto) = {
			val row = ProductImagesRow(i.productId, -1, i.image, i.sequence)
			db run (ProductImages+=row)
			Future(i)
		}
		
		def insertOption(o: ProductOptionDto) = o.itemList match {
			case Nil => Future.failed(new Exception("error"))
			case _ =>
				val query = (ProductOptions returning ProductOptions.map(_.productOptionId))
				val row = ProductOptionsRow(o.productId, 0, o.name, o.optionSequence)
				for { optionId <- db.run(query += row) } yield o.setOptionId(optionId)
		}
		
		def insertItem(i: ProductOptionItemDto) = {
			val row = ProductOptionItemsRow(i.productOptionId, -1, i.name, i.itemSequence, i.surcharge)
			db.run(ProductOptionItems += row)
			Future(i)
		}
		
		def getStocks(productId: Int, depth: Int, parentId: Int): Future[List[StockResponseDto]] = {
			val query = ProductStock.filter(s => s.productId === productId && s.depth === depth)
			val f = (s: ProductStock#TableElementType) =>
				StockResponseDto(s.productId, s.stock, s.productStockId, s.parentId, s.name, s.depth, Nil)
			parentId match {
				case 0 => db.run(query.result) map(_.toList map f)
				case pid: Int => db.run(query.filter(s => s.parentId === parentId).result) map(_.toList map(f))
			}
		}
	}
	
	import InnerApi._
	
	def getProductsWithAll(implicit f: Products => Rep[Boolean]): Future[List[ProductDto]] =
		getProducts(getOptions(_, getItems))
	
	def getProductsWithAllSortBy(page: Int, n: Int, h: Products => slick.lifted.ColumnOrdered[_])
								(implicit f: Products => Rep[Boolean]): Future[List[ProductDto]] =
		getProductsSortBy(page, n, getOptions(_, getItems), h)
	
	def getProductsCount(implicit f: Products => Rep[Boolean]): Future[Int] =
		db.run(Products.filter(f(_)).map(_.productId).result).map(_.toList.size)
		
	def getProductById(productId: Int): Future[ProductDto] =
		getProduct(productId, getOptions(_, getItems) flatMap(getImages))
		
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
	
	def getProductStock(productId: Int): Future[List[StockResponseDto]] = {
		def go(depth: Int, cs: StockResponseDto): Future[StockResponseDto] = {
			getStocks(productId, depth, cs.productStockId) flatMap {
				case Nil => Future(Nil)
				case h::t => (h::t) traverse (go(depth+1, _))
			} map cs.setList
		}
		getStocks(productId, 0, 0) flatMap (_ traverse(go(1, _)))
	}
	
	def insertProductStock(p: ProductDto): Future[_] = {
		def go(os: List[ProductOptionDto], pid: Int, depth: Int): Future[_] = os match {
			case h :: t => h.itemList traverse { item =>
				val query = (ProductStock returning ProductStock.map(_.productStockId))
				val row = ProductStockRow(p.productId, 0, 0, pid, item.name, depth)
				db run (query += row) map (go(t, _, depth + 1)) }
			case Nil => Future(pid)
		}
		go(p.optionList, 0, 0)
	}
	
	def updateStock(stockId: Int, adds: Int): Future[Int] = {
		def go(stockId: Int, affRows: Int): Future[Int] = {
			val ci = ProductStock.filter(_.productStockId === stockId)
			db run (for {
				stockOption <- ci.map(_.stock).result.headOption
				updateOption = stockOption.map(stock => ci.map(_.stock).update(stock + adds))
				affected <- updateOption.getOrElse(DBIO.successful(0))
			} yield affected) flatMap { aff =>
				db run ci.map(_.parentId).result.headOption flatMap ({
					case Some(parentId) => go(parentId, affRows+aff)
					case None => Future(affRows+aff)
				})
			}
		}
		go(stockId, 0)
	}
}

