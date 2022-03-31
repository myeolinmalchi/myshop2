package dto

case class StockRequestDto(productId: Int, depth: Int)
case class StockDto(productId: Int,
					stock: Int,
					productStockId: Int,
					parentId: Int,
					var name: String,
					depth: Int) {
	
	def setName(name: String): StockDto = {
		this.name = name
		this
	}
	
}

case class StockResponseDto(productId: Int,
					stock: Int,
					productStockId: Int,
					parentId: Int,
					var name: String,
					depth: Int, var stockDto: List[StockResponseDto]) {
	
	def setName(name: String): StockResponseDto = {
		this.name = name
		this
	}
	
	def setList(list: List[StockResponseDto]): StockResponseDto = {
		this.stockDto = list
		this
	}
	
}
