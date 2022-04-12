package models

import cats.implicits._
import dto._
import javax.inject._
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

@Singleton
class ProductModel @Inject() (val dbConfigProvider: DatabaseConfigProvider)
							 (implicit ec: ExecutionContext)
	extends HasDatabaseConfigProvider[JdbcProfile] {
	
	private object InnerApi {
		
		def insertProduct(p: ProductDto) = {
			val query = Products returning Products.map(_.productId)
			val row = ProductsRow(p.productId, p.name, p.sellerId,
				p.price, p.categoryCode, p.detailInfo, p.thumbnail)
			query += row
		}
		
		def insertOption(pid: Int, o: ProductOptionDto) = {
			val query = ProductOptions returning ProductOptions.map(_.productOptionId)
			val row = ProductOptionsRow(pid, 0, o.name, o.optionSequence)
			query += row
		}
		
		def insertItem(oid: Int, i: ProductOptionItemDto) = {
			val query = ProductOptionItems returning ProductOptionItems.map(_.productOptionItemId)
			val row = ProductOptionItemsRow(oid, 0, i.name, i.itemSequence, i.surcharge)
			query += row
		}
		
		def insertOptions(pid: Int, os: List[ProductOptionDto]) = DBIO.sequence {
			os map { o =>
				for {
					optionId <- insertOption(pid, o)
					itemDtoList <- insertItems(pid, optionId.self, o.itemList)
				} yield o.setItems(itemDtoList)
			}
		}
		
		def insertItems(pid: Int, oid: Int, is: List[ProductOptionItemDto]) = DBIO.sequence {
			is map { i =>
				for {
					itemId <- insertItem(oid, i)
				} yield i.setId(itemId.self)
			}
		}
		
		def insertImages(pid: Int, is: List[ProductImageDto]) = {
			val rows = is.map(i => ProductImagesRow(pid, 0, i.image, i.sequence))
			ProductImages ++= rows
		}
		
		def initProductStock(pid: Int, os: List[ProductOptionDto]): DBIOAction[Int, NoStream, Effect.All]= {
			def go(os: List[ProductOptionDto],
				   parentId: Int, depth: Int): DBIOAction[Int, NoStream, Effect.All]= os match {
				case h :: t => DBIO.sequence { h.itemList map { item =>
					val query = ProductStock returning ProductStock.map(_.productStockId)
					val row = ProductStockRow(pid, 0, 0, parentId, item.productOptionItemId, depth)
					(for {
						stockId <- query += row
						aff <- go(t, stockId.self, depth + 1)
					} yield aff).transactionally
				}} map(_.sum)
				case Nil => DBIO.successful(0)
			}
			go(os, 0, 0)
		}
		
		val productQuery = (f: Products => Rep[Boolean]) => Products.filter(f(_))
		val optionQuery = (pid: Int) => ProductOptions.filter(_.productId === pid)
		val itemQuery = (oid: Int) => ProductOptionItems.filter(_.productOptionId === oid)
		val imageQuery = (pid: Int) => ProductImages.filter(_.productId === pid)
		
		def toDto[T, R](xs: Seq[T])(g: R => DBIOAction[R, NoStream, Effect.All])
					   (implicit f: T => R): DBIOAction[List[R], NoStream, Effect.All] =
			DBIO.sequence(xs.map(f andThen g).toList)
	}

	import InnerApi._
	
	def getProducts(implicit f: Products => Rep[Boolean]): Future[List[ProductDto]] =
		db run (for {
			products <- productQuery(f).result
			productDtoList <- toDto(products) { p: ProductDto =>
				for{options <- optionQuery(p.productId).result
					optionDtoList <- toDto(options) { o: ProductOptionDto =>
						for{items <- itemQuery(o.productOptionId).result
							itemDtoList = items.map(ProductOptionItemDto.newInstance).toList
						} yield o.setItems(itemDtoList) }
				} yield p.setOptions(optionDtoList) }
		} yield productDtoList)
		
	def getProductsSortBy(page: Int, n: Int, h: Products => slick.lifted.ColumnOrdered[_])
						 (implicit f: Products => Rep[Boolean]): Future[List[ProductDto]] =
		db run (for {
			products <- productQuery(f).sortBy(h).drop((page-1)*n).take(n).result
			productDtoList <- toDto(products) { p: ProductDto =>
				for{options <- optionQuery(p.productId).result
					optionDtoList <- toDto(options) { o: ProductOptionDto =>
						for{items <- itemQuery(o.productOptionId).result
							itemDtoList = items.map(ProductOptionItemDto.newInstance).toList
						} yield o.setItems(itemDtoList) }
				} yield p.setOptions(optionDtoList) }
		} yield productDtoList)
	
	def getProductsCount(implicit f: Products => Rep[Boolean]): Future[Int] =
		db run Products.filter(f(_)).map(_.productId).result map(_.toList.size)
	
	def getProductOptionsCount(productId: Int): Future[Int] =
		db run ProductOptions.filter(o => o.productId === productId)
				.map(_.productOptionId).result map(_.toList.size)
		
	def getProductByIdQuery(productId: Int): DBIOAction[ProductDto, NoStream, Effect.All] =
		for {
			product <- productQuery(_.productId === productId).result.headOption
			options <- optionQuery(productId).result
			images <- imageQuery(productId).result
			productDto = ProductDto.newInstance(product.getOrElse(throw new Exception()))
			optionDtoList <- toDto(options) { o: ProductOptionDto =>
				for{items <- itemQuery(o.productOptionId).result
					itemDtoList <- toDto(items) { i: ProductOptionItemDto =>  DBIO.successful(i) }
					} yield o.setItems(itemDtoList) }
			imageDtoList <- toDto(images) { i: ProductImageDto => DBIO.successful(i) }
		} yield productDto
				.setOptions(optionDtoList)
				.setImages(imageDtoList)
	
	def getProductById(productId: Int): Future[ProductDto]=
		db run getProductByIdQuery(productId)
	
	def insertProductWithAll(p: ProductDto): Future[Int] =
		db run (for {
			productId <- insertProduct(p)
			optionDtoList <- insertOptions(productId.self, p.optionList)
			aff1 <- initProductStock(productId.self, optionDtoList)
			aff2 <- insertImages(productId.self, p.imageList)
		} yield aff1.self + aff2.getOrElse(0)).transactionally
	
	def getProductOptionStock(productId: Int, depth: Int, parentId: Int): Future[List[StockResponseDto]] = {
		val query = ProductStock.filter(s => s.productId === productId && s.depth === depth)
		val f = (s: ProductStock#TableElementType) =>
			StockResponseDto(s.productId, s.stock.toInt, s.productStockId, s.parentId, s.productOptionItemId, s.depth,
				Nil)
		parentId match {
			case 0 => db.run(query.result) map(_.toList map f)
			case pid: Int => db.run(query.filter(s => s.parentId === parentId).result) map(_.toList map f)
		}
	}
	
	def getStockIdQuery(is: List[Int]): DBIOAction[Int, NoStream, Effect.All] = {
		def go(is: List[Int], parentId: Int): DBIOAction[Int, NoStream, Effect.All] = is match {
			case h:: t =>
				val query = ProductStock.filter { stock =>
					stock.productOptionItemId === h && stock.parentId === parentId }
				(for {
					stockIdOption <- query.map(_.productStockId).result.headOption
					stockId = stockIdOption.getOrElse(throw new Exception())
				} yield go(t, stockId)).flatten
			case Nil => DBIO.successful(parentId)
		}
		go(is, 0)
	}
	
	def getStockId(is: List[Int]): Future[Int] =
		db run getStockIdQuery(is)
	
	def checkStockQuery(is: List[Int], quantity: Int): DBIOAction[(Int, Boolean),NoStream, Effect.All] =  {
		def go(is: List[Int], parentId: Int, s: Int): DBIOAction[(Int, Boolean),NoStream,Effect.All] = is match {
			case h::t =>
				val query = ProductStock.filter { stock =>
					stock.productOptionItemId === h && stock.parentId === parentId }
				(for {
					stockOption <- query.result.headOption
					stockRow = stockOption.getOrElse(throw new Exception())
				} yield {
					stockRow.stock match {
						case stock if stock >= quantity =>
							go(t, stockRow.productStockId, stock.toInt)
						case stock if stock < quantity =>
							DBIO.successful((stock.toInt, false))
						case _ => DBIO.successful((0, false))
					}
				}).flatten
			case Nil => DBIO.successful((s, true))
		}
		go(is, 0, 0)
	}
	
	def checkStock(is: List[Int], quantity: Int): Future[(Int, Boolean)] =
		db run checkStockQuery(is, quantity)
	
	def getProductStock(productId: Int): Future[List[StockResponseDto]] = {
		def go(depth: Int, cs: StockResponseDto): Future[StockResponseDto] = {
			getProductOptionStock(productId, depth, cs.productStockId) flatMap {
				case Nil => Future(Nil)
				case h::t => (h::t) traverse (go(depth+1, _))
			} map cs.setList
		}
		getProductOptionStock(productId, 0, 0) flatMap (_ traverse(go(1, _)))
	}
	
	def updateStockQuery(stockId: Int, adds: Int): DBIOAction[Int,NoStream,Effect.All] = {
		def go(stockId: Int, affRows: Int): DBIOAction[Int,NoStream,Effect.All] = {
			val temp = ProductStock.filter(_.productStockId === stockId)
			(for {
				stockOption <- temp.map(_.stock).result.headOption
				updateOption = stockOption.map(stock => temp.map(_.stock).update(stock + adds))
				affected <- updateOption.getOrElse(DBIO.successful(0))
				parentIdOption <- affected match {
					case aff: Int if aff == 1 =>
						temp.map(_.parentId).result.headOption
					case _ => DBIO.successful(None) }
			} yield {
				parentIdOption match {
					case Some(parentId) => go(parentId, affRows + affected)
					case None => DBIO.successful(affRows)
				}
			}).transactionally.flatten
		}
		go(stockId, 0)
	}
	
	def updateStock(stockId: Int, adds: Int): Future[Int] =
		db run updateStockQuery(stockId, adds)
	
}

