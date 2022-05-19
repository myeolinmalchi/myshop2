package common.validation

import cats.implicits.toTraverseOps
import common.validation.ValidationResultLib.ValidationFailure
import dto.CartRequestDto
import models.ProductModel
import scala.concurrent.{ExecutionContext, Future}

object CartValidationLib {
	sealed abstract class CartInsertionFailure extends ValidationFailure
	case class OutOfStock(stock: Int) extends CartInsertionFailure
	case object IncorrectProductId extends CartInsertionFailure
	case class IncorrectItemSize(size: Int) extends CartInsertionFailure
	case object ProductNotExists extends CartInsertionFailure
}

trait CartValidationLib extends ValidationResultLib[Future] {
	import CartValidationLib._
	
	def checkProductExists(cart: CartRequestDto)
												(implicit productModel: ProductModel,
												 ex: ExecutionContext): ValidationResult[CartInsertionFailure, Unit] =
		ValidationResult.ensureM(
			productModel checkProductExists cart.productId,
			onFailure = ProductNotExists
		)
	
	private def checkCorrectProductId(itemId: Int, productId: Int)
													 (implicit productModel: ProductModel,
														ex: ExecutionContext): Future[Boolean] =
		productModel getProductIdByItemId itemId map {
			case Some(pid) if pid == productId => true
			case Some(_) => false
			case None => false
		}
	
	def checkAllCorrectProductId(cart: CartRequestDto)
															(implicit productModel: ProductModel,
															 ex: ExecutionContext): ValidationResult[CartInsertionFailure, Unit] =
		ValidationResult.ensureM(
			cart.itemList traverse { itemId =>
				checkCorrectProductId(itemId, cart.productId)
			} map (_.forall(is => is)),
			onFailure = IncorrectProductId
		)
	
	def checkStock(cart: CartRequestDto)
								(implicit productModel: ProductModel,
								 ex: ExecutionContext): ValidationResult[CartInsertionFailure, Unit] =
		ValidationResult(
			productModel getStockByItemList cart.itemList flatMap { stock =>
				ValidationResult.ensure(
					stock >= cart.quantity,
					onFailure = OutOfStock(stock)
				).value
			}
		)
	
	def checkOptionCount(cart: CartRequestDto)
											(implicit productModel: ProductModel,
											 ex: ExecutionContext): ValidationResult[CartInsertionFailure, Unit] =
		ValidationResult(
			productModel getProductOptionsCount cart.productId flatMap { size =>
				ValidationResult.ensure(
					size == cart.itemList.size,
					onFailure = IncorrectItemSize(size)
				).value
			}
		)
	
}
