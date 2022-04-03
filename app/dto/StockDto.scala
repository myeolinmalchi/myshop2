package dto

import play.api.libs.json.Json

case class StockRequestDto(productId: Int, depth: Int)
case class StockDto(productId: Int,
					stock: Int,
					productStockId: Int,
					parentId: Int,
					var itemId: Int,
					depth: Int) {
	
	def setItemId(id: Int): StockDto = {
		this.itemId = id
		this
	}
	
}

case class StockResponseDto(productId: Int,
					stock: Int,
					productStockId: Int,
					parentId: Int,
					var itemId: Int,
					depth: Int, var stockDto: List[StockResponseDto]) {
	
	def setItemId(id: Int): StockResponseDto = {
		this.itemId = id
		this
	}
	
	def setList(list: List[StockResponseDto]): StockResponseDto = {
		this.stockDto = list
		this
	}
	
}

object StockDto {
	implicit val stockReads = Json.reads[StockDto]
	implicit val stockWrites = Json.writes[StockDto]
}
object StockResponseDto {
	implicit val stockResponseWrite = Json.writes[StockResponseDto]
	implicit val stockREsponseRead = Json.reads[StockResponseDto]
}