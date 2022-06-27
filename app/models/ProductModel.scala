package models

import cats.data.OptionT
import cats.implicits._
import dto._
import javax.inject._
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

@Singleton
class ProductModel @Inject()(val dbConfigProvider: DatabaseConfigProvider)
														(implicit ec: ExecutionContext)
	extends HasDatabaseConfigProvider[JdbcProfile] {
	
	private object InnerApi {
		
		def initProductStock(pid: Int, os: List[ProductOptionDto]): DBIOAction[Int, NoStream, Effect.All] = {
			def go(os: List[ProductOptionDto],
						 parentId: Int, depth: Int): DBIOAction[Int, NoStream, Effect.All] = os match {
				case h :: t => DBIO.sequence {
					h.itemList map { item =>
						val query = ProductStock returning ProductStock.map(_.productStockId)
						val row = ProductStockRow(pid, 0, 0, parentId, item.productOptionItemId.getOrElse(0), depth)
						(for {
							stockId <- query += row
							aff <- go(t, stockId.self, depth + 1)
						} yield aff).transactionally
					}
				} map (_.sum)
				case Nil => DBIO.successful(0)
			}
			
			go(os, 0, 0)
		}
		
		val getProductsWithFilterQuery = (f: Products => Rep[Boolean]) => Products.filter(f(_))
		val getOptionsByProductIdQuery = (pid: Int) => ProductOptions.filter(_.productId === pid)
		val getItemsByOptionIdQuery = (oid: Int) => ProductOptionItems.filter(_.productOptionId === oid)
		val getImagesByProductIdQuery = (pid: Int) => ProductImages.filter(_.productId === pid)
		
		def toDto[T, R](xs: Seq[T])(g: R => DBIOAction[R, NoStream, Effect.All])
									 (implicit f: T => R): DBIOAction[List[R], NoStream, Effect.All] =
			DBIO.sequence(xs.map(f andThen g).toList)
		
	}
	
	import InnerApi._
	
	def getProductIdByItemId(itemId: Int): Future[Option[Int]] =
		db run (for {
			optionIdOption <- ProductOptionItems
				.filter(_.productOptionItemId === itemId)
				.map(_.productOptionId)
				.result
				.headOption
			productIdOption <- optionIdOption.map { optionId =>
				ProductOptions
					.filter(_.productOptionId === optionId)
					.map(_.productId)
					.result
					.headOption
			} getOrElse DBIOAction.successful(None)
		} yield productIdOption)
	
	def getProducts(implicit f: Products => Rep[Boolean]): Future[List[ProductDto]] =
		db run (for {
			products <- getProductsWithFilterQuery(f).result
			productDtoList <- toDto(products) { p: ProductDto =>
				for {
					options <- getOptionsByProductIdQuery(p.productId.getOrElse(0)).result
					optionDtoList <- toDto(options) { o: ProductOptionDto =>
						for {
							items <- getItemsByOptionIdQuery(o.productOptionId.getOrElse(0)).result
							itemDtoList = items.map(ProductOptionItemDto.newInstance).toList
						} yield o.setItems(itemDtoList)
					}
				} yield p.setOptions(optionDtoList)
			}
		} yield productDtoList)
		
	def searchProductsOrderBy(keyword: String,
														category: String,
														page: Int,
														size: Int,
														orderBy: String): Future[List[ProductDto]] = db run {
		for {
			products <- sql"""
				SELECT * FROM v_products
				WHERE name LIKE '%#$keyword%' AND category_code LIKE '%#$category%'
				ORDER BY $orderBy
				LIMIT $size
				OFFSET ${(page-1)*size}
			""".as[ProductDto]
			result <- DBIOAction.sequence(products.toList map { p =>
				for {
					options <- getOptionsByProductIdQuery(p.productId.get).result
					optionDtoList <- toDto(options) { o: ProductOptionDto =>
						for {
							items <- getItemsByOptionIdQuery(o.productOptionId.get).result
							itemDtoList = items.map(ProductOptionItemDto.newInstance).toList
						} yield o.setItems(itemDtoList)
					}
				} yield p.setOptions(optionDtoList)
			})
		} yield result
	}
	
	def getRandomProducts(size: Int): Future[List[ProductDto]] = db run {
		for {
			products <- sql"""
				SELECT * FROM v_products
				ORDER BY RAND()
				LIMIT $size
			""".as[ProductDto]
			result <- DBIOAction.sequence(products.toList map { p =>
				for {
					options <- getOptionsByProductIdQuery(p.productId.get).result
					optionDtoList <- toDto(options) { o: ProductOptionDto =>
						for {
							items <- getItemsByOptionIdQuery(o.productOptionId.get).result
							itemDtoList = items.map(ProductOptionItemDto.newInstance).toList
						} yield o.setItems(itemDtoList)
					}
				} yield p.setOptions(optionDtoList)
			})
		} yield result
	}
	
	def checkProductExists(productId: Int): Future[Boolean] =
		db run {
			Products
				.filter(_.productId === productId)
				.map(_.productId)
				.result
				.headOption map {
				case Some(_) => true
				case None => false
			}
		}
	
	def getProductsCount(implicit f: Products => Rep[Boolean]): Future[Int] =
		db run Products.filter(f(_)).map(_.productId).result map (_.toList.size)
	
	def getProductOptionsCount(productId: Int): Future[Int] =
		db run ProductOptions.filter(o => o.productId === productId)
			.map(_.productOptionId).result map (_.toList.size)
	
	def getSellerByProductId(productId: Int): Future[Option[String]] =
		db run Products.filter(_.productId === productId).map(_.sellerId).result.headOption
	
	def getProductByIdQuery(productId: Int): DBIOAction[Option[ProductDto], NoStream, Effect.All] =
		for {
			productOption <-
				sql"SELECT * FROM v_products WHERE product_id = ${productId}".as[ProductDto].headOption
			options <- getOptionsByProductIdQuery(productId).result
			images <- getImagesByProductIdQuery(productId).result
			optionDtoList <- toDto(options) { o: ProductOptionDto =>
				for {
					items <- getItemsByOptionIdQuery(o.productOptionId.get).result
					itemDtoList <- toDto(items) { i: ProductOptionItemDto => DBIO.successful(i) }
				} yield o.setItems(itemDtoList)
			}
			imageDtoList <- toDto(images) { i: ProductImageDto => DBIO.successful(i) }
		} yield productOption map { productDto =>
			productDto
				.setOptions(optionDtoList)
				.setImages(imageDtoList)
		}
	
	def getProductById(productId: Int): OptionT[Future, ProductDto] =
		OptionT(db run getProductByIdQuery(productId))
	
	def insertProductWithAll(p: ProductDto): Future[Int] =
		db run (for {
			productId <- Products returning Products.map(_.productId) +=
				ProductsRow(0, p.name, p.sellerId, p.price, p.categoryCode, p.detailInfo, p.thumbnail)
			optionList <- DBIO.sequence(
				p.optionList map { option =>
					for {
						optionId <- ProductOptions returning ProductOptions.map(_.productOptionId) +=
							ProductOptionsRow(productId, 0, option.name, option.optionSequence)
						itemList <- DBIO.sequence(option.itemList map { item =>
							for {
								itemId <- ProductOptionItems returning ProductOptionItems.map(_.productOptionItemId) +=
									ProductOptionItemsRow(optionId, 0, item.name, item.itemSequence, item.surcharge)
							} yield item.setId(itemId)
						})
					} yield option.setItems(itemList)
				}
			)
			aff2 <- ProductImages ++= p.imageList map { image =>
				ProductImagesRow(productId, 0, image.image, image.sequence)
			}
			aff3 <- initProductStock(productId, optionList)
		} yield aff2.getOrElse(0) + aff3).transactionally
	
	def getProductOptionStock(productId: Int, depth: Int, parentId: Int): Future[List[StockResponseDto]] = {
		val query = ProductStock.filter(s => s.productId === productId && s.depth === depth)
		val f = (s: ProductStock#TableElementType) =>
			StockResponseDto(s.productId, s.stock.toInt, s.productStockId, s.parentId, s.productOptionItemId, s.depth,
				Nil)
		parentId match {
			case 0 => db.run(query.result) map (_.toList map f)
			case pid: Int => db.run(query.filter(s => s.parentId === parentId).result) map (_.toList map f)
		}
	}
	
	def getStockIdQuery(is: List[Int]): DBIOAction[Int, NoStream, Effect.All] = {
		def go(is: List[Int], parentId: Int): DBIOAction[Int, NoStream, Effect.All] = is match {
			case h :: t =>
				val query = ProductStock.filter { stock =>
					stock.productOptionItemId === h && stock.parentId === parentId
				}
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
	
	//	남은 재고가 있는지 검사하는 쿼리
	def checkStockQuery(is: List[Int], quantity: Int): DBIOAction[(Int, Boolean), NoStream, Effect.All] = {
		def go(is: List[Int], parentId: Int, s: Int): DBIOAction[(Int, Boolean), NoStream, Effect.All] = is match {
			case h :: t =>
				val query = ProductStock.filter { stock =>
					stock.productOptionItemId === h && stock.parentId === parentId
				}
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
	
	def getStockByItemList(is: List[Int]): Future[Int] = {
		def go(is: List[Int], parentId: Int, s: Int): DBIOAction[Int, NoStream, Effect.All] = is match {
			case h :: t =>
				val query = ProductStock.filter { stock =>
					stock.productOptionItemId === h && stock.parentId === parentId
				}
				(for {
					stockOption <- query.result.headOption
					stockRow = stockOption.getOrElse(throw new Exception())
				} yield go(t, stockRow.productStockId, stockRow.stock.toInt)).flatten
			case Nil => DBIO.successful(s)
		}
		
		is match {
			case h :: t => db run go(h :: t, 0, 0)
			case Nil => throw new NoSuchElementException
		}
	}
	
	def getProductStock(productId: Int): Future[List[StockResponseDto]] = {
		def go(depth: Int, cs: StockResponseDto): Future[StockResponseDto] = {
			getProductOptionStock(productId, depth, cs.productStockId) flatMap {
				case Nil => Future(Nil)
				case h :: t => (h :: t) traverse (go(depth + 1, _))
			} map cs.setList
		}
		
		getProductOptionStock(productId, 0, 0) flatMap (_ traverse (go(1, _)))
	}
	
	def updateStockQuery(stockId: Int, adds: Int): DBIOAction[Int, NoStream, Effect.All] = {
		def go(stockId: Int, affRows: Int): DBIOAction[Int, NoStream, Effect.All] = {
			val temp = ProductStock.filter(_.productStockId === stockId)
			(for {
				stockOption <- temp.map(_.stock).result.headOption
				updateOption = stockOption.map(stock => temp.map(_.stock).update(stock + adds))
				affected <- updateOption.getOrElse(DBIO.successful(0))
				parentIdOption <- affected match {
					case aff: Int if aff == 1 =>
						temp.map(_.parentId).result.headOption
					case _ => DBIO.successful(None)
				}
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
	
	def getStockProductId(stockId: Int): Future[Option[Int]] =
		db run ProductStock.filter(_.productStockId === stockId).map(_.productId).result.headOption
	
}

