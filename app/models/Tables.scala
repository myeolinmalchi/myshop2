package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.MySQLProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(CartDetails.schema, Carts.schema, Categories.schema, OrderDetails.schema, Orders.schema, ProductImages.schema, ProductOptionItems.schema, ProductOptions.schema, Products.schema, ProductStock.schema, Qnas.schema, Reviews.schema, Sellers.schema, TempProductOptionItems.schema, TempProductOptions.schema, TempProducts.schema, UserAddresses.schema, Users.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table CartDetails
   *  @param cartDetailId Database column cart_detail_id SqlType(INT), AutoInc, PrimaryKey
   *  @param cartId Database column cart_id SqlType(INT), Default(None)
   *  @param optionItemId Database column option_item_id SqlType(INT), Default(None) */
  case class CartDetailsRow(cartDetailId: Int, cartId: Option[Int] = None, optionItemId: Option[Int] = None)
  /** GetResult implicit for fetching CartDetailsRow objects using plain SQL queries */
  implicit def GetResultCartDetailsRow(implicit e0: GR[Int], e1: GR[Option[Int]]): GR[CartDetailsRow] = GR{
    prs => import prs._
    CartDetailsRow.tupled((<<[Int], <<?[Int], <<?[Int]))
  }
  /** Table description of table cart_details. Objects of this class serve as prototypes for rows in queries. */
  class CartDetails(_tableTag: Tag) extends profile.api.Table[CartDetailsRow](_tableTag, Some("myshop2"), "cart_details") {
    def * = (cartDetailId, cartId, optionItemId) <> (CartDetailsRow.tupled, CartDetailsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cartDetailId), cartId, optionItemId)).shaped.<>({r=>import r._; _1.map(_=> CartDetailsRow.tupled((_1.get, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column cart_detail_id SqlType(INT), AutoInc, PrimaryKey */
    val cartDetailId: Rep[Int] = column[Int]("cart_detail_id", O.AutoInc, O.PrimaryKey)
    /** Database column cart_id SqlType(INT), Default(None) */
    val cartId: Rep[Option[Int]] = column[Option[Int]]("cart_id", O.Default(None))
    /** Database column option_item_id SqlType(INT), Default(None) */
    val optionItemId: Rep[Option[Int]] = column[Option[Int]]("option_item_id", O.Default(None))

    /** Foreign key referencing Carts (database name cart_details_ibfk_1) */
    lazy val cartsFk = foreignKey("cart_details_ibfk_1", cartId, Carts)(r => Rep.Some(r.cartId), onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table CartDetails */
  lazy val CartDetails = new TableQuery(tag => new CartDetails(tag))

  /** Entity class storing rows of table Carts
   *  @param userId Database column user_id SqlType(VARCHAR), Length(20,true)
   *  @param cartId Database column cart_id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(100,true)
   *  @param productId Database column product_id SqlType(INT)
   *  @param price Database column price SqlType(INT)
   *  @param quantity Database column quantity SqlType(INT)
   *  @param addedDate Database column added_date SqlType(DATETIME) */
  case class CartsRow(userId: String, cartId: Int, name: String, productId: Int, price: Int, quantity: Int, addedDate: java.sql.Timestamp)
  /** GetResult implicit for fetching CartsRow objects using plain SQL queries */
  implicit def GetResultCartsRow(implicit e0: GR[String], e1: GR[Int], e2: GR[java.sql.Timestamp]): GR[CartsRow] = GR{
    prs => import prs._
    CartsRow.tupled((<<[String], <<[Int], <<[String], <<[Int], <<[Int], <<[Int], <<[java.sql.Timestamp]))
  }
  /** Table description of table carts. Objects of this class serve as prototypes for rows in queries. */
  class Carts(_tableTag: Tag) extends profile.api.Table[CartsRow](_tableTag, Some("myshop2"), "carts") {
    def * = (userId, cartId, name, productId, price, quantity, addedDate) <> (CartsRow.tupled, CartsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(cartId), Rep.Some(name), Rep.Some(productId), Rep.Some(price), Rep.Some(quantity), Rep.Some(addedDate))).shaped.<>({r=>import r._; _1.map(_=> CartsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(VARCHAR), Length(20,true) */
    val userId: Rep[String] = column[String]("user_id", O.Length(20,varying=true))
    /** Database column cart_id SqlType(INT), AutoInc, PrimaryKey */
    val cartId: Rep[Int] = column[Int]("cart_id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(100,true) */
    val name: Rep[String] = column[String]("name", O.Length(100,varying=true))
    /** Database column product_id SqlType(INT) */
    val productId: Rep[Int] = column[Int]("product_id")
    /** Database column price SqlType(INT) */
    val price: Rep[Int] = column[Int]("price")
    /** Database column quantity SqlType(INT) */
    val quantity: Rep[Int] = column[Int]("quantity")
    /** Database column added_date SqlType(DATETIME) */
    val addedDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("added_date")

    /** Foreign key referencing Products (database name carts_ibfk_2) */
    lazy val productsFk = foreignKey("carts_ibfk_2", productId, Products)(r => r.productId, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
    /** Foreign key referencing Users (database name carts_ibfk_1) */
    lazy val usersFk = foreignKey("carts_ibfk_1", userId, Users)(r => r.userId, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table Carts */
  lazy val Carts = new TableQuery(tag => new Carts(tag))

  /** Entity class storing rows of table Categories
   *  @param categoryId Database column category_id SqlType(INT), AutoInc, PrimaryKey
   *  @param groupId Database column group_id SqlType(VARCHAR), Length(10,true), Default(None)
   *  @param name Database column name SqlType(VARCHAR), Length(500,true), Default(None)
   *  @param depth Database column depth SqlType(INT), Default(None)
   *  @param sequence Database column sequence SqlType(INT), Default(None)
   *  @param parentDepth Database column parent_depth SqlType(INT), Default(None)
   *  @param parentSequence Database column parent_sequence SqlType(INT), Default(None) */
  case class CategoriesRow(categoryId: Int, groupId: Option[String] = None, name: Option[String] = None, depth: Option[Int] = None, sequence: Option[Int] = None, parentDepth: Option[Int] = None, parentSequence: Option[Int] = None)
  /** GetResult implicit for fetching CategoriesRow objects using plain SQL queries */
  implicit def GetResultCategoriesRow(implicit e0: GR[Int], e1: GR[Option[String]], e2: GR[Option[Int]]): GR[CategoriesRow] = GR{
    prs => import prs._
    CategoriesRow.tupled((<<[Int], <<?[String], <<?[String], <<?[Int], <<?[Int], <<?[Int], <<?[Int]))
  }
  /** Table description of table categories. Objects of this class serve as prototypes for rows in queries. */
  class Categories(_tableTag: Tag) extends profile.api.Table[CategoriesRow](_tableTag, Some("myshop2"), "categories") {
    def * = (categoryId, groupId, name, depth, sequence, parentDepth, parentSequence) <> (CategoriesRow.tupled, CategoriesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(categoryId), groupId, name, depth, sequence, parentDepth, parentSequence)).shaped.<>({r=>import r._; _1.map(_=> CategoriesRow.tupled((_1.get, _2, _3, _4, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column category_id SqlType(INT), AutoInc, PrimaryKey */
    val categoryId: Rep[Int] = column[Int]("category_id", O.AutoInc, O.PrimaryKey)
    /** Database column group_id SqlType(VARCHAR), Length(10,true), Default(None) */
    val groupId: Rep[Option[String]] = column[Option[String]]("group_id", O.Length(10,varying=true), O.Default(None))
    /** Database column name SqlType(VARCHAR), Length(500,true), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Length(500,varying=true), O.Default(None))
    /** Database column depth SqlType(INT), Default(None) */
    val depth: Rep[Option[Int]] = column[Option[Int]]("depth", O.Default(None))
    /** Database column sequence SqlType(INT), Default(None) */
    val sequence: Rep[Option[Int]] = column[Option[Int]]("sequence", O.Default(None))
    /** Database column parent_depth SqlType(INT), Default(None) */
    val parentDepth: Rep[Option[Int]] = column[Option[Int]]("parent_depth", O.Default(None))
    /** Database column parent_sequence SqlType(INT), Default(None) */
    val parentSequence: Rep[Option[Int]] = column[Option[Int]]("parent_sequence", O.Default(None))
  }
  /** Collection-like TableQuery object for table Categories */
  lazy val Categories = new TableQuery(tag => new Categories(tag))

  /** Entity class storing rows of table OrderDetails
   *  @param orderId Database column order_id SqlType(INT), Default(None)
   *  @param orderDetailId Database column order_detail_id SqlType(INT), AutoInc, PrimaryKey
   *  @param productId Database column product_id SqlType(INT), Default(None)
   *  @param name Database column name SqlType(VARCHAR), Length(200,true)
   *  @param quantity Database column quantity SqlType(SMALLINT), Default(None)
   *  @param orderDetailStatus Database column order_detail_status SqlType(SMALLINT), Default(None) */
  case class OrderDetailsRow(orderId: Option[Int] = None, orderDetailId: Int, productId: Option[Int] = None, name: String, quantity: Option[Int] = None, orderDetailStatus: Option[Int] = None)
  /** GetResult implicit for fetching OrderDetailsRow objects using plain SQL queries */
  implicit def GetResultOrderDetailsRow(implicit e0: GR[Option[Int]], e1: GR[Int], e2: GR[String]): GR[OrderDetailsRow] = GR{
    prs => import prs._
    OrderDetailsRow.tupled((<<?[Int], <<[Int], <<?[Int], <<[String], <<?[Int], <<?[Int]))
  }
  /** Table description of table order_details. Objects of this class serve as prototypes for rows in queries. */
  class OrderDetails(_tableTag: Tag) extends profile.api.Table[OrderDetailsRow](_tableTag, Some("myshop2"), "order_details") {
    def * = (orderId, orderDetailId, productId, name, quantity, orderDetailStatus) <> (OrderDetailsRow.tupled, OrderDetailsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((orderId, Rep.Some(orderDetailId), productId, Rep.Some(name), quantity, orderDetailStatus)).shaped.<>({r=>import r._; _2.map(_=> OrderDetailsRow.tupled((_1, _2.get, _3, _4.get, _5, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column order_id SqlType(INT), Default(None) */
    val orderId: Rep[Option[Int]] = column[Option[Int]]("order_id", O.Default(None))
    /** Database column order_detail_id SqlType(INT), AutoInc, PrimaryKey */
    val orderDetailId: Rep[Int] = column[Int]("order_detail_id", O.AutoInc, O.PrimaryKey)
    /** Database column product_id SqlType(INT), Default(None) */
    val productId: Rep[Option[Int]] = column[Option[Int]]("product_id", O.Default(None))
    /** Database column name SqlType(VARCHAR), Length(200,true) */
    val name: Rep[String] = column[String]("name", O.Length(200,varying=true))
    /** Database column quantity SqlType(SMALLINT), Default(None) */
    val quantity: Rep[Option[Int]] = column[Option[Int]]("quantity", O.Default(None))
    /** Database column order_detail_status SqlType(SMALLINT), Default(None) */
    val orderDetailStatus: Rep[Option[Int]] = column[Option[Int]]("order_detail_status", O.Default(None))

    /** Foreign key referencing Orders (database name order_details_ibfk_2) */
    lazy val ordersFk = foreignKey("order_details_ibfk_2", orderId, Orders)(r => Rep.Some(r.orderId), onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Products (database name order_details_ibfk_1) */
    lazy val productsFk = foreignKey("order_details_ibfk_1", productId, Products)(r => Rep.Some(r.productId), onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table OrderDetails */
  lazy val OrderDetails = new TableQuery(tag => new OrderDetails(tag))

  /** Entity class storing rows of table Orders
   *  @param userId Database column user_id SqlType(VARCHAR), Length(20,true), Default(None)
   *  @param orderId Database column order_id SqlType(INT), AutoInc, PrimaryKey
   *  @param orderDate Database column order_date SqlType(DATETIME)
   *  @param orderStatus Database column order_status SqlType(SMALLINT), Default(None) */
  case class OrdersRow(userId: Option[String] = None, orderId: Int, orderDate: Option[java.sql.Timestamp], orderStatus: Option[Int] = None)
  /** GetResult implicit for fetching OrdersRow objects using plain SQL queries */
  implicit def GetResultOrdersRow(implicit e0: GR[Option[String]], e1: GR[Int], e2: GR[Option[java.sql.Timestamp]], e3: GR[Option[Int]]): GR[OrdersRow] = GR{
    prs => import prs._
    OrdersRow.tupled((<<?[String], <<[Int], <<?[java.sql.Timestamp], <<?[Int]))
  }
  /** Table description of table orders. Objects of this class serve as prototypes for rows in queries. */
  class Orders(_tableTag: Tag) extends profile.api.Table[OrdersRow](_tableTag, Some("myshop2"), "orders") {
    def * = (userId, orderId, orderDate, orderStatus) <> (OrdersRow.tupled, OrdersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((userId, Rep.Some(orderId), orderDate, orderStatus)).shaped.<>({r=>import r._; _2.map(_=> OrdersRow.tupled((_1, _2.get, _3, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(VARCHAR), Length(20,true), Default(None) */
    val userId: Rep[Option[String]] = column[Option[String]]("user_id", O.Length(20,varying=true), O.Default(None))
    /** Database column order_id SqlType(INT), AutoInc, PrimaryKey */
    val orderId: Rep[Int] = column[Int]("order_id", O.AutoInc, O.PrimaryKey)
    /** Database column order_date SqlType(DATETIME) */
    val orderDate: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("order_date")
    /** Database column order_status SqlType(SMALLINT), Default(None) */
    val orderStatus: Rep[Option[Int]] = column[Option[Int]]("order_status", O.Default(None))

    /** Foreign key referencing Users (database name orders_ibfk_1) */
    lazy val usersFk = foreignKey("orders_ibfk_1", userId, Users)(r => Rep.Some(r.userId), onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table Orders */
  lazy val Orders = new TableQuery(tag => new Orders(tag))

  /** Entity class storing rows of table ProductImages
   *  @param productId Database column product_id SqlType(INT)
   *  @param productImageId Database column product_image_id SqlType(INT), AutoInc, PrimaryKey
   *  @param image Database column image SqlType(LONGTEXT), Length(2147483647,true)
   *  @param sequence Database column sequence SqlType(INT) */
  case class ProductImagesRow(productId: Int, productImageId: Int, image: String, sequence: Int)
  /** GetResult implicit for fetching ProductImagesRow objects using plain SQL queries */
  implicit def GetResultProductImagesRow(implicit e0: GR[Int], e1: GR[String]): GR[ProductImagesRow] = GR{
    prs => import prs._
    ProductImagesRow.tupled((<<[Int], <<[Int], <<[String], <<[Int]))
  }
  /** Table description of table product_images. Objects of this class serve as prototypes for rows in queries. */
  class ProductImages(_tableTag: Tag) extends profile.api.Table[ProductImagesRow](_tableTag, Some("myshop2"), "product_images") {
    def * = (productId, productImageId, image, sequence) <> (ProductImagesRow.tupled, ProductImagesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(productId), Rep.Some(productImageId), Rep.Some(image), Rep.Some(sequence))).shaped.<>({r=>import r._; _1.map(_=> ProductImagesRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column product_id SqlType(INT) */
    val productId: Rep[Int] = column[Int]("product_id")
    /** Database column product_image_id SqlType(INT), AutoInc, PrimaryKey */
    val productImageId: Rep[Int] = column[Int]("product_image_id", O.AutoInc, O.PrimaryKey)
    /** Database column image SqlType(LONGTEXT), Length(2147483647,true) */
    val image: Rep[String] = column[String]("image", O.Length(2147483647,varying=true))
    /** Database column sequence SqlType(INT) */
    val sequence: Rep[Int] = column[Int]("sequence")

    /** Foreign key referencing Products (database name FK_product_images_products) */
    lazy val productsFk = foreignKey("FK_product_images_products", productId, Products)(r => r.productId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table ProductImages */
  lazy val ProductImages = new TableQuery(tag => new ProductImages(tag))

  /** Entity class storing rows of table ProductOptionItems
   *  @param productOptionId Database column product_option_id SqlType(INT)
   *  @param productOptionItemId Database column product_option_item_id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(50,true)
   *  @param itemSequence Database column item_sequence SqlType(INT)
   *  @param surcharge Database column surcharge SqlType(INT)
   *  @param stock Database column stock SqlType(INT) */
  case class ProductOptionItemsRow(productOptionId: Int, productOptionItemId: Int, name: String, itemSequence: Int, surcharge: Int, stock: Int)
  /** GetResult implicit for fetching ProductOptionItemsRow objects using plain SQL queries */
  implicit def GetResultProductOptionItemsRow(implicit e0: GR[Int], e1: GR[String]): GR[ProductOptionItemsRow] = GR{
    prs => import prs._
    ProductOptionItemsRow.tupled((<<[Int], <<[Int], <<[String], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table product_option_items. Objects of this class serve as prototypes for rows in queries. */
  class ProductOptionItems(_tableTag: Tag) extends profile.api.Table[ProductOptionItemsRow](_tableTag, Some("myshop2"), "product_option_items") {
    def * = (productOptionId, productOptionItemId, name, itemSequence, surcharge, stock) <> (ProductOptionItemsRow.tupled, ProductOptionItemsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(productOptionId), Rep.Some(productOptionItemId), Rep.Some(name), Rep.Some(itemSequence), Rep.Some(surcharge), Rep.Some(stock))).shaped.<>({r=>import r._; _1.map(_=> ProductOptionItemsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column product_option_id SqlType(INT) */
    val productOptionId: Rep[Int] = column[Int]("product_option_id")
    /** Database column product_option_item_id SqlType(INT), AutoInc, PrimaryKey */
    val productOptionItemId: Rep[Int] = column[Int]("product_option_item_id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(50,true) */
    val name: Rep[String] = column[String]("name", O.Length(50,varying=true))
    /** Database column item_sequence SqlType(INT) */
    val itemSequence: Rep[Int] = column[Int]("item_sequence")
    /** Database column surcharge SqlType(INT) */
    val surcharge: Rep[Int] = column[Int]("surcharge")
    /** Database column stock SqlType(INT) */
    val stock: Rep[Int] = column[Int]("stock")

    /** Foreign key referencing ProductOptions (database name product_option_items_ibfk_1) */
    lazy val productOptionsFk = foreignKey("product_option_items_ibfk_1", productOptionId, ProductOptions)(r => r.productOptionId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table ProductOptionItems */
  lazy val ProductOptionItems = new TableQuery(tag => new ProductOptionItems(tag))

  /** Entity class storing rows of table ProductOptions
   *  @param productId Database column product_id SqlType(INT)
   *  @param productOptionId Database column product_option_id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(40,true)
   *  @param optionSequence Database column option_sequence SqlType(INT) */
  case class ProductOptionsRow(productId: Int, productOptionId: Int, name: String, optionSequence: Int)
  /** GetResult implicit for fetching ProductOptionsRow objects using plain SQL queries */
  implicit def GetResultProductOptionsRow(implicit e0: GR[Int], e1: GR[String]): GR[ProductOptionsRow] = GR{
    prs => import prs._
    ProductOptionsRow.tupled((<<[Int], <<[Int], <<[String], <<[Int]))
  }
  /** Table description of table product_options. Objects of this class serve as prototypes for rows in queries. */
  class ProductOptions(_tableTag: Tag) extends profile.api.Table[ProductOptionsRow](_tableTag, Some("myshop2"), "product_options") {
    def * = (productId, productOptionId, name, optionSequence) <> (ProductOptionsRow.tupled, ProductOptionsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(productId), Rep.Some(productOptionId), Rep.Some(name), Rep.Some(optionSequence))).shaped.<>({r=>import r._; _1.map(_=> ProductOptionsRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column product_id SqlType(INT) */
    val productId: Rep[Int] = column[Int]("product_id")
    /** Database column product_option_id SqlType(INT), AutoInc, PrimaryKey */
    val productOptionId: Rep[Int] = column[Int]("product_option_id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(40,true) */
    val name: Rep[String] = column[String]("name", O.Length(40,varying=true))
    /** Database column option_sequence SqlType(INT) */
    val optionSequence: Rep[Int] = column[Int]("option_sequence")

    /** Foreign key referencing Products (database name product_options_ibfk_1) */
    lazy val productsFk = foreignKey("product_options_ibfk_1", productId, Products)(r => r.productId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table ProductOptions */
  lazy val ProductOptions = new TableQuery(tag => new ProductOptions(tag))

  /** Entity class storing rows of table Products
   *  @param productId Database column product_id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(40,true)
   *  @param sellerId Database column seller_id SqlType(VARCHAR), Length(20,true)
   *  @param price Database column price SqlType(INT UNSIGNED), Default(0)
   *  @param categoryCode Database column category_code SqlType(VARCHAR), Length(20,true), Default()
   *  @param detailInfo Database column detail_info SqlType(VARCHAR), Length(200,true), Default()
   *  @param thumbnail Database column thumbnail SqlType(LONGTEXT), Length(2147483647,true)
   *  @param reviewCount Database column review_count SqlType(INT), Default(0)
   *  @param rating Database column rating SqlType(INT), Default(0) */
  case class ProductsRow(productId: Int, name: String, sellerId: String, price: Long = 0L, categoryCode: String = "", detailInfo: String = "", thumbnail: String, reviewCount: Int = 0, rating: Int = 0)
  /** GetResult implicit for fetching ProductsRow objects using plain SQL queries */
  implicit def GetResultProductsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Long]): GR[ProductsRow] = GR{
    prs => import prs._
    ProductsRow.tupled((<<[Int], <<[String], <<[String], <<[Long], <<[String], <<[String], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table products. Objects of this class serve as prototypes for rows in queries. */
  class Products(_tableTag: Tag) extends profile.api.Table[ProductsRow](_tableTag, Some("myshop2"), "products") {
    def * = (productId, name, sellerId, price, categoryCode, detailInfo, thumbnail, reviewCount, rating) <> (ProductsRow.tupled, ProductsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(productId), Rep.Some(name), Rep.Some(sellerId), Rep.Some(price), Rep.Some(categoryCode), Rep.Some(detailInfo), Rep.Some(thumbnail), Rep.Some(reviewCount), Rep.Some(rating))).shaped.<>({r=>import r._; _1.map(_=> ProductsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column product_id SqlType(INT), AutoInc, PrimaryKey */
    val productId: Rep[Int] = column[Int]("product_id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(40,true) */
    val name: Rep[String] = column[String]("name", O.Length(40,varying=true))
    /** Database column seller_id SqlType(VARCHAR), Length(20,true) */
    val sellerId: Rep[String] = column[String]("seller_id", O.Length(20,varying=true))
    /** Database column price SqlType(INT UNSIGNED), Default(0) */
    val price: Rep[Long] = column[Long]("price", O.Default(0L))
    /** Database column category_code SqlType(VARCHAR), Length(20,true), Default() */
    val categoryCode: Rep[String] = column[String]("category_code", O.Length(20,varying=true), O.Default(""))
    /** Database column detail_info SqlType(VARCHAR), Length(200,true), Default() */
    val detailInfo: Rep[String] = column[String]("detail_info", O.Length(200,varying=true), O.Default(""))
    /** Database column thumbnail SqlType(LONGTEXT), Length(2147483647,true) */
    val thumbnail: Rep[String] = column[String]("thumbnail", O.Length(2147483647,varying=true))
    /** Database column review_count SqlType(INT), Default(0) */
    val reviewCount: Rep[Int] = column[Int]("review_count", O.Default(0))
    /** Database column rating SqlType(INT), Default(0) */
    val rating: Rep[Int] = column[Int]("rating", O.Default(0))

    /** Foreign key referencing Sellers (database name products_ibfk_1) */
    lazy val sellersFk = foreignKey("products_ibfk_1", sellerId, Sellers)(r => r.sellerId, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)

    /** Index over (categoryCode) (database name category_id) */
    val index1 = index("category_id", categoryCode)
  }
  /** Collection-like TableQuery object for table Products */
  lazy val Products = new TableQuery(tag => new Products(tag))

  /** Entity class storing rows of table ProductStock
   *  @param productStockId Database column product_stock_id SqlType(INT), AutoInc, PrimaryKey
   *  @param parentId Database column parent_id SqlType(INT)
   *  @param name Database column name SqlType(VARCHAR), Length(50,true)
   *  @param depth Database column depth SqlType(INT) */
  case class ProductStockRow(productStockId: Int, parentId: Int, name: String, depth: Int)
  /** GetResult implicit for fetching ProductStockRow objects using plain SQL queries */
  implicit def GetResultProductStockRow(implicit e0: GR[Int], e1: GR[String]): GR[ProductStockRow] = GR{
    prs => import prs._
    ProductStockRow.tupled((<<[Int], <<[Int], <<[String], <<[Int]))
  }
  /** Table description of table product_stock. Objects of this class serve as prototypes for rows in queries. */
  class ProductStock(_tableTag: Tag) extends profile.api.Table[ProductStockRow](_tableTag, Some("myshop2"), "product_stock") {
    def * = (productStockId, parentId, name, depth) <> (ProductStockRow.tupled, ProductStockRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(productStockId), Rep.Some(parentId), Rep.Some(name), Rep.Some(depth))).shaped.<>({r=>import r._; _1.map(_=> ProductStockRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column product_stock_id SqlType(INT), AutoInc, PrimaryKey */
    val productStockId: Rep[Int] = column[Int]("product_stock_id", O.AutoInc, O.PrimaryKey)
    /** Database column parent_id SqlType(INT) */
    val parentId: Rep[Int] = column[Int]("parent_id")
    /** Database column name SqlType(VARCHAR), Length(50,true) */
    val name: Rep[String] = column[String]("name", O.Length(50,varying=true))
    /** Database column depth SqlType(INT) */
    val depth: Rep[Int] = column[Int]("depth")
  }
  /** Collection-like TableQuery object for table ProductStock */
  lazy val ProductStock = new TableQuery(tag => new ProductStock(tag))

  /** Entity class storing rows of table Qnas
   *  @param productId Database column product_id SqlType(INT), Default(None)
   *  @param qnaId Database column qna_id SqlType(INT), AutoInc, PrimaryKey
   *  @param userId Database column user_id SqlType(VARCHAR), Length(20,true), Default(None)
   *  @param question Database column question SqlType(VARCHAR), Length(500,true), Default(None)
   *  @param answer Database column answer SqlType(VARCHAR), Length(500,true), Default(None) */
  case class QnasRow(productId: Option[Int] = None, qnaId: Int, userId: Option[String] = None, question: Option[String] = None, answer: Option[String] = None)
  /** GetResult implicit for fetching QnasRow objects using plain SQL queries */
  implicit def GetResultQnasRow(implicit e0: GR[Option[Int]], e1: GR[Int], e2: GR[Option[String]]): GR[QnasRow] = GR{
    prs => import prs._
    QnasRow.tupled((<<?[Int], <<[Int], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table qnas. Objects of this class serve as prototypes for rows in queries. */
  class Qnas(_tableTag: Tag) extends profile.api.Table[QnasRow](_tableTag, Some("myshop2"), "qnas") {
    def * = (productId, qnaId, userId, question, answer) <> (QnasRow.tupled, QnasRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((productId, Rep.Some(qnaId), userId, question, answer)).shaped.<>({r=>import r._; _2.map(_=> QnasRow.tupled((_1, _2.get, _3, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column product_id SqlType(INT), Default(None) */
    val productId: Rep[Option[Int]] = column[Option[Int]]("product_id", O.Default(None))
    /** Database column qna_id SqlType(INT), AutoInc, PrimaryKey */
    val qnaId: Rep[Int] = column[Int]("qna_id", O.AutoInc, O.PrimaryKey)
    /** Database column user_id SqlType(VARCHAR), Length(20,true), Default(None) */
    val userId: Rep[Option[String]] = column[Option[String]]("user_id", O.Length(20,varying=true), O.Default(None))
    /** Database column question SqlType(VARCHAR), Length(500,true), Default(None) */
    val question: Rep[Option[String]] = column[Option[String]]("question", O.Length(500,varying=true), O.Default(None))
    /** Database column answer SqlType(VARCHAR), Length(500,true), Default(None) */
    val answer: Rep[Option[String]] = column[Option[String]]("answer", O.Length(500,varying=true), O.Default(None))

    /** Foreign key referencing Products (database name qnas_ibfk_2) */
    lazy val productsFk = foreignKey("qnas_ibfk_2", productId, Products)(r => Rep.Some(r.productId), onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
    /** Foreign key referencing Users (database name qnas_ibfk_1) */
    lazy val usersFk = foreignKey("qnas_ibfk_1", userId, Users)(r => Rep.Some(r.userId), onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table Qnas */
  lazy val Qnas = new TableQuery(tag => new Qnas(tag))

  /** Entity class storing rows of table Reviews
   *  @param productId Database column product_id SqlType(INT), Default(None)
   *  @param reviewId Database column review_id SqlType(INT), AutoInc, PrimaryKey
   *  @param userId Database column user_id SqlType(VARCHAR), Length(20,true), Default(None)
   *  @param rating Database column rating SqlType(SMALLINT), Default(None)
   *  @param title Database column title SqlType(VARCHAR), Length(200,true), Default(None)
   *  @param content Database column content SqlType(VARCHAR), Length(2000,true)
   *  @param comment Database column comment SqlType(VARCHAR), Length(500,true), Default(None)
   *  @param reviewDate Database column review_date SqlType(DATE), Default(None)
   *  @param recommend Database column recommend SqlType(INT), Default(None) */
  case class ReviewsRow(productId: Option[Int] = None, reviewId: Int, userId: Option[String] = None, rating: Option[Int] = None, title: Option[String] = None, content: String, comment: Option[String] = None, reviewDate: Option[java.sql.Date] = None, recommend: Option[Int] = None)
  /** GetResult implicit for fetching ReviewsRow objects using plain SQL queries */
  implicit def GetResultReviewsRow(implicit e0: GR[Option[Int]], e1: GR[Int], e2: GR[Option[String]], e3: GR[String], e4: GR[Option[java.sql.Date]]): GR[ReviewsRow] = GR{
    prs => import prs._
    ReviewsRow.tupled((<<?[Int], <<[Int], <<?[String], <<?[Int], <<?[String], <<[String], <<?[String], <<?[java.sql.Date], <<?[Int]))
  }
  /** Table description of table reviews. Objects of this class serve as prototypes for rows in queries. */
  class Reviews(_tableTag: Tag) extends profile.api.Table[ReviewsRow](_tableTag, Some("myshop2"), "reviews") {
    def * = (productId, reviewId, userId, rating, title, content, comment, reviewDate, recommend) <> (ReviewsRow.tupled, ReviewsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((productId, Rep.Some(reviewId), userId, rating, title, Rep.Some(content), comment, reviewDate, recommend)).shaped.<>({r=>import r._; _2.map(_=> ReviewsRow.tupled((_1, _2.get, _3, _4, _5, _6.get, _7, _8, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column product_id SqlType(INT), Default(None) */
    val productId: Rep[Option[Int]] = column[Option[Int]]("product_id", O.Default(None))
    /** Database column review_id SqlType(INT), AutoInc, PrimaryKey */
    val reviewId: Rep[Int] = column[Int]("review_id", O.AutoInc, O.PrimaryKey)
    /** Database column user_id SqlType(VARCHAR), Length(20,true), Default(None) */
    val userId: Rep[Option[String]] = column[Option[String]]("user_id", O.Length(20,varying=true), O.Default(None))
    /** Database column rating SqlType(SMALLINT), Default(None) */
    val rating: Rep[Option[Int]] = column[Option[Int]]("rating", O.Default(None))
    /** Database column title SqlType(VARCHAR), Length(200,true), Default(None) */
    val title: Rep[Option[String]] = column[Option[String]]("title", O.Length(200,varying=true), O.Default(None))
    /** Database column content SqlType(VARCHAR), Length(2000,true) */
    val content: Rep[String] = column[String]("content", O.Length(2000,varying=true))
    /** Database column comment SqlType(VARCHAR), Length(500,true), Default(None) */
    val comment: Rep[Option[String]] = column[Option[String]]("comment", O.Length(500,varying=true), O.Default(None))
    /** Database column review_date SqlType(DATE), Default(None) */
    val reviewDate: Rep[Option[java.sql.Date]] = column[Option[java.sql.Date]]("review_date", O.Default(None))
    /** Database column recommend SqlType(INT), Default(None) */
    val recommend: Rep[Option[Int]] = column[Option[Int]]("recommend", O.Default(None))

    /** Foreign key referencing Products (database name reviews_ibfk_2) */
    lazy val productsFk = foreignKey("reviews_ibfk_2", productId, Products)(r => Rep.Some(r.productId), onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
    /** Foreign key referencing Users (database name reviews_ibfk_1) */
    lazy val usersFk = foreignKey("reviews_ibfk_1", userId, Users)(r => Rep.Some(r.userId), onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Reviews */
  lazy val Reviews = new TableQuery(tag => new Reviews(tag))

  /** Entity class storing rows of table Sellers
   *  @param sellerId Database column seller_id SqlType(VARCHAR), PrimaryKey, Length(20,true)
   *  @param sellerPw Database column seller_pw SqlType(VARCHAR), Length(100,true)
   *  @param name Database column name SqlType(VARCHAR), Length(20,true)
   *  @param email Database column email SqlType(VARCHAR), Length(40,true)
   *  @param phonenumber Database column phonenumber SqlType(VARCHAR), Length(20,true)
   *  @param regdate Database column regdate SqlType(TIMESTAMP) */
  case class SellersRow(sellerId: String, sellerPw: String, name: String, email: String, phonenumber: String, regdate: java.sql.Timestamp)
  /** GetResult implicit for fetching SellersRow objects using plain SQL queries */
  implicit def GetResultSellersRow(implicit e0: GR[String], e1: GR[java.sql.Timestamp]): GR[SellersRow] = GR{
    prs => import prs._
    SellersRow.tupled((<<[String], <<[String], <<[String], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table sellers. Objects of this class serve as prototypes for rows in queries. */
  class Sellers(_tableTag: Tag) extends profile.api.Table[SellersRow](_tableTag, Some("myshop2"), "sellers") {
    def * = (sellerId, sellerPw, name, email, phonenumber, regdate) <> (SellersRow.tupled, SellersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(sellerId), Rep.Some(sellerPw), Rep.Some(name), Rep.Some(email), Rep.Some(phonenumber), Rep.Some(regdate))).shaped.<>({r=>import r._; _1.map(_=> SellersRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column seller_id SqlType(VARCHAR), PrimaryKey, Length(20,true) */
    val sellerId: Rep[String] = column[String]("seller_id", O.PrimaryKey, O.Length(20,varying=true))
    /** Database column seller_pw SqlType(VARCHAR), Length(100,true) */
    val sellerPw: Rep[String] = column[String]("seller_pw", O.Length(100,varying=true))
    /** Database column name SqlType(VARCHAR), Length(20,true) */
    val name: Rep[String] = column[String]("name", O.Length(20,varying=true))
    /** Database column email SqlType(VARCHAR), Length(40,true) */
    val email: Rep[String] = column[String]("email", O.Length(40,varying=true))
    /** Database column phonenumber SqlType(VARCHAR), Length(20,true) */
    val phonenumber: Rep[String] = column[String]("phonenumber", O.Length(20,varying=true))
    /** Database column regdate SqlType(TIMESTAMP) */
    val regdate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("regdate")
  }
  /** Collection-like TableQuery object for table Sellers */
  lazy val Sellers = new TableQuery(tag => new Sellers(tag))

  /** Entity class storing rows of table TempProductOptionItems
   *  @param tempProductOptionId Database column temp_product_option_id SqlType(INT)
   *  @param tempProductOptionItemId Database column temp_product_option_item_id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(500,true), Default(None)
   *  @param itemSequence Database column item_sequence SqlType(INT), Default(None)
   *  @param surcharge Database column surcharge SqlType(INT), Default(None) */
  case class TempProductOptionItemsRow(tempProductOptionId: Int, tempProductOptionItemId: Int, name: Option[String] = None, itemSequence: Option[Int] = None, surcharge: Option[Int] = None)
  /** GetResult implicit for fetching TempProductOptionItemsRow objects using plain SQL queries */
  implicit def GetResultTempProductOptionItemsRow(implicit e0: GR[Int], e1: GR[Option[String]], e2: GR[Option[Int]]): GR[TempProductOptionItemsRow] = GR{
    prs => import prs._
    TempProductOptionItemsRow.tupled((<<[Int], <<[Int], <<?[String], <<?[Int], <<?[Int]))
  }
  /** Table description of table temp_product_option_items. Objects of this class serve as prototypes for rows in queries. */
  class TempProductOptionItems(_tableTag: Tag) extends profile.api.Table[TempProductOptionItemsRow](_tableTag, Some("myshop2"), "temp_product_option_items") {
    def * = (tempProductOptionId, tempProductOptionItemId, name, itemSequence, surcharge) <> (TempProductOptionItemsRow.tupled, TempProductOptionItemsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(tempProductOptionId), Rep.Some(tempProductOptionItemId), name, itemSequence, surcharge)).shaped.<>({r=>import r._; _1.map(_=> TempProductOptionItemsRow.tupled((_1.get, _2.get, _3, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column temp_product_option_id SqlType(INT) */
    val tempProductOptionId: Rep[Int] = column[Int]("temp_product_option_id")
    /** Database column temp_product_option_item_id SqlType(INT), AutoInc, PrimaryKey */
    val tempProductOptionItemId: Rep[Int] = column[Int]("temp_product_option_item_id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(500,true), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Length(500,varying=true), O.Default(None))
    /** Database column item_sequence SqlType(INT), Default(None) */
    val itemSequence: Rep[Option[Int]] = column[Option[Int]]("item_sequence", O.Default(None))
    /** Database column surcharge SqlType(INT), Default(None) */
    val surcharge: Rep[Option[Int]] = column[Option[Int]]("surcharge", O.Default(None))

    /** Foreign key referencing TempProductOptions (database name temp_product_option_items_ibfk_1) */
    lazy val tempProductOptionsFk = foreignKey("temp_product_option_items_ibfk_1", tempProductOptionId, TempProductOptions)(r => r.tempProductOptionId, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table TempProductOptionItems */
  lazy val TempProductOptionItems = new TableQuery(tag => new TempProductOptionItems(tag))

  /** Entity class storing rows of table TempProductOptions
   *  @param tempProductId Database column temp_product_id SqlType(INT)
   *  @param tempProductOptionId Database column temp_product_option_id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(100,true), Default(None)
   *  @param optionSequence Database column option_sequence SqlType(INT), Default(None)
   *  @param images Database column images SqlType(VARCHAR), Length(1000,true), Default(None) */
  case class TempProductOptionsRow(tempProductId: Int, tempProductOptionId: Int, name: Option[String] = None, optionSequence: Option[Int] = None, images: Option[String] = None)
  /** GetResult implicit for fetching TempProductOptionsRow objects using plain SQL queries */
  implicit def GetResultTempProductOptionsRow(implicit e0: GR[Int], e1: GR[Option[String]], e2: GR[Option[Int]]): GR[TempProductOptionsRow] = GR{
    prs => import prs._
    TempProductOptionsRow.tupled((<<[Int], <<[Int], <<?[String], <<?[Int], <<?[String]))
  }
  /** Table description of table temp_product_options. Objects of this class serve as prototypes for rows in queries. */
  class TempProductOptions(_tableTag: Tag) extends profile.api.Table[TempProductOptionsRow](_tableTag, Some("myshop2"), "temp_product_options") {
    def * = (tempProductId, tempProductOptionId, name, optionSequence, images) <> (TempProductOptionsRow.tupled, TempProductOptionsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(tempProductId), Rep.Some(tempProductOptionId), name, optionSequence, images)).shaped.<>({r=>import r._; _1.map(_=> TempProductOptionsRow.tupled((_1.get, _2.get, _3, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column temp_product_id SqlType(INT) */
    val tempProductId: Rep[Int] = column[Int]("temp_product_id")
    /** Database column temp_product_option_id SqlType(INT), AutoInc, PrimaryKey */
    val tempProductOptionId: Rep[Int] = column[Int]("temp_product_option_id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(100,true), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Length(100,varying=true), O.Default(None))
    /** Database column option_sequence SqlType(INT), Default(None) */
    val optionSequence: Rep[Option[Int]] = column[Option[Int]]("option_sequence", O.Default(None))
    /** Database column images SqlType(VARCHAR), Length(1000,true), Default(None) */
    val images: Rep[Option[String]] = column[Option[String]]("images", O.Length(1000,varying=true), O.Default(None))

    /** Foreign key referencing TempProducts (database name temp_product_options_ibfk_1) */
    lazy val tempProductsFk = foreignKey("temp_product_options_ibfk_1", tempProductId, TempProducts)(r => r.tempProductId, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table TempProductOptions */
  lazy val TempProductOptions = new TableQuery(tag => new TempProductOptions(tag))

  /** Entity class storing rows of table TempProducts
   *  @param tempProductId Database column temp_product_id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(100,true), Default(None)
   *  @param sellerId Database column seller_id SqlType(VARCHAR), Length(100,true), Default(None)
   *  @param price Database column price SqlType(INT), Default(None)
   *  @param categoryCode Database column category_code SqlType(VARCHAR), Length(20,true), Default(None)
   *  @param detailInfo Database column detail_info SqlType(VARCHAR), Length(1000,true), Default(None)
   *  @param thumbnail Database column thumbnail SqlType(VARCHAR), Length(1000,true), Default(None) */
  case class TempProductsRow(tempProductId: Int, name: Option[String] = None, sellerId: Option[String] = None, price: Option[Int] = None, categoryCode: Option[String] = None, detailInfo: Option[String] = None, thumbnail: Option[String] = None)
  /** GetResult implicit for fetching TempProductsRow objects using plain SQL queries */
  implicit def GetResultTempProductsRow(implicit e0: GR[Int], e1: GR[Option[String]], e2: GR[Option[Int]]): GR[TempProductsRow] = GR{
    prs => import prs._
    TempProductsRow.tupled((<<[Int], <<?[String], <<?[String], <<?[Int], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table temp_products. Objects of this class serve as prototypes for rows in queries. */
  class TempProducts(_tableTag: Tag) extends profile.api.Table[TempProductsRow](_tableTag, Some("myshop2"), "temp_products") {
    def * = (tempProductId, name, sellerId, price, categoryCode, detailInfo, thumbnail) <> (TempProductsRow.tupled, TempProductsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(tempProductId), name, sellerId, price, categoryCode, detailInfo, thumbnail)).shaped.<>({r=>import r._; _1.map(_=> TempProductsRow.tupled((_1.get, _2, _3, _4, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column temp_product_id SqlType(INT), AutoInc, PrimaryKey */
    val tempProductId: Rep[Int] = column[Int]("temp_product_id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(100,true), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Length(100,varying=true), O.Default(None))
    /** Database column seller_id SqlType(VARCHAR), Length(100,true), Default(None) */
    val sellerId: Rep[Option[String]] = column[Option[String]]("seller_id", O.Length(100,varying=true), O.Default(None))
    /** Database column price SqlType(INT), Default(None) */
    val price: Rep[Option[Int]] = column[Option[Int]]("price", O.Default(None))
    /** Database column category_code SqlType(VARCHAR), Length(20,true), Default(None) */
    val categoryCode: Rep[Option[String]] = column[Option[String]]("category_code", O.Length(20,varying=true), O.Default(None))
    /** Database column detail_info SqlType(VARCHAR), Length(1000,true), Default(None) */
    val detailInfo: Rep[Option[String]] = column[Option[String]]("detail_info", O.Length(1000,varying=true), O.Default(None))
    /** Database column thumbnail SqlType(VARCHAR), Length(1000,true), Default(None) */
    val thumbnail: Rep[Option[String]] = column[Option[String]]("thumbnail", O.Length(1000,varying=true), O.Default(None))

    /** Foreign key referencing Sellers (database name temp_products_ibfk_1) */
    lazy val sellersFk = foreignKey("temp_products_ibfk_1", sellerId, Sellers)(r => Rep.Some(r.sellerId), onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table TempProducts */
  lazy val TempProducts = new TableQuery(tag => new TempProducts(tag))

  /** Entity class storing rows of table UserAddresses
   *  @param userId Database column user_id SqlType(VARCHAR), Length(20,true)
   *  @param addressId Database column address_id SqlType(INT), AutoInc, PrimaryKey
   *  @param priority Database column priority SqlType(SMALLINT)
   *  @param address Database column address SqlType(VARCHAR), Length(200,true)
   *  @param addressDetail Database column address_detail SqlType(VARCHAR), Length(200,true)
   *  @param zipcode Database column zipcode SqlType(SMALLINT) */
  case class UserAddressesRow(userId: String, addressId: Int, priority: Int, address: String, addressDetail: String, zipcode: Int)
  /** GetResult implicit for fetching UserAddressesRow objects using plain SQL queries */
  implicit def GetResultUserAddressesRow(implicit e0: GR[String], e1: GR[Int]): GR[UserAddressesRow] = GR{
    prs => import prs._
    UserAddressesRow.tupled((<<[String], <<[Int], <<[Int], <<[String], <<[String], <<[Int]))
  }
  /** Table description of table user_addresses. Objects of this class serve as prototypes for rows in queries. */
  class UserAddresses(_tableTag: Tag) extends profile.api.Table[UserAddressesRow](_tableTag, Some("myshop2"), "user_addresses") {
    def * = (userId, addressId, priority, address, addressDetail, zipcode) <> (UserAddressesRow.tupled, UserAddressesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(addressId), Rep.Some(priority), Rep.Some(address), Rep.Some(addressDetail), Rep.Some(zipcode))).shaped.<>({r=>import r._; _1.map(_=> UserAddressesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(VARCHAR), Length(20,true) */
    val userId: Rep[String] = column[String]("user_id", O.Length(20,varying=true))
    /** Database column address_id SqlType(INT), AutoInc, PrimaryKey */
    val addressId: Rep[Int] = column[Int]("address_id", O.AutoInc, O.PrimaryKey)
    /** Database column priority SqlType(SMALLINT) */
    val priority: Rep[Int] = column[Int]("priority")
    /** Database column address SqlType(VARCHAR), Length(200,true) */
    val address: Rep[String] = column[String]("address", O.Length(200,varying=true))
    /** Database column address_detail SqlType(VARCHAR), Length(200,true) */
    val addressDetail: Rep[String] = column[String]("address_detail", O.Length(200,varying=true))
    /** Database column zipcode SqlType(SMALLINT) */
    val zipcode: Rep[Int] = column[Int]("zipcode")

    /** Foreign key referencing Users (database name user_addresses_ibfk_1) */
    lazy val usersFk = foreignKey("user_addresses_ibfk_1", userId, Users)(r => r.userId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)

    /** Uniqueness Index over (address,addressDetail) (database name address) */
    val index1 = index("address", (address, addressDetail), unique=true)
  }
  /** Collection-like TableQuery object for table UserAddresses */
  lazy val UserAddresses = new TableQuery(tag => new UserAddresses(tag))

  /** Entity class storing rows of table Users
   *  @param userId Database column user_id SqlType(VARCHAR), PrimaryKey, Length(20,true)
   *  @param userPw Database column user_pw SqlType(VARCHAR), Length(100,true)
   *  @param name Database column name SqlType(VARCHAR), Length(20,true)
   *  @param email Database column email SqlType(VARCHAR), Length(40,true)
   *  @param phonenumber Database column phonenumber SqlType(VARCHAR), Length(20,true)
   *  @param regdate Database column regdate SqlType(TIMESTAMP) */
  case class UsersRow(userId: String, userPw: String, name: String, email: String, phonenumber: String, regdate: Option[java.sql.Timestamp])
  /** GetResult implicit for fetching UsersRow objects using plain SQL queries */
  implicit def GetResultUsersRow(implicit e0: GR[String], e1: GR[Option[java.sql.Timestamp]]): GR[UsersRow] = GR{
    prs => import prs._
    UsersRow.tupled((<<[String], <<[String], <<[String], <<[String], <<[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table users. Objects of this class serve as prototypes for rows in queries. */
  class Users(_tableTag: Tag) extends profile.api.Table[UsersRow](_tableTag, Some("myshop2"), "users") {
    def * = (userId, userPw, name, email, phonenumber, regdate) <> (UsersRow.tupled, UsersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(userPw), Rep.Some(name), Rep.Some(email), Rep.Some(phonenumber), regdate)).shaped.<>({r=>import r._; _1.map(_=> UsersRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(VARCHAR), PrimaryKey, Length(20,true) */
    val userId: Rep[String] = column[String]("user_id", O.PrimaryKey, O.Length(20,varying=true))
    /** Database column user_pw SqlType(VARCHAR), Length(100,true) */
    val userPw: Rep[String] = column[String]("user_pw", O.Length(100,varying=true))
    /** Database column name SqlType(VARCHAR), Length(20,true) */
    val name: Rep[String] = column[String]("name", O.Length(20,varying=true))
    /** Database column email SqlType(VARCHAR), Length(40,true) */
    val email: Rep[String] = column[String]("email", O.Length(40,varying=true))
    /** Database column phonenumber SqlType(VARCHAR), Length(20,true) */
    val phonenumber: Rep[String] = column[String]("phonenumber", O.Length(20,varying=true))
    /** Database column regdate SqlType(TIMESTAMP) */
    val regdate: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("regdate")

    /** Uniqueness Index over (email) (database name email) */
    val index1 = index("email", email, unique=true)
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))
}
