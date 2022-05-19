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
  lazy val schema: profile.SchemaDescription = Array(CartDetails.schema, Carts.schema, Categories.schema, NonUserCartDetails.schema, NonUserCarts.schema, OrderProductDetails.schema, OrderProducts.schema, Orders.schema, ProductImages.schema, ProductOptionItems.schema, ProductOptions.schema, Products.schema, ProductStock.schema, Qnas.schema, ReviewImages.schema, Reviews.schema, Sellers.schema, UserAddresses.schema, Users.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table CartDetails
   *  @param cartDetailId Database column cart_detail_id SqlType(INT), AutoInc, PrimaryKey
   *  @param cartId Database column cart_id SqlType(INT)
   *  @param optionItemId Database column option_item_id SqlType(INT) */
  case class CartDetailsRow(cartDetailId: Int, cartId: Int, optionItemId: Int)
  /** GetResult implicit for fetching CartDetailsRow objects using plain SQL queries */
  implicit def GetResultCartDetailsRow(implicit e0: GR[Int]): GR[CartDetailsRow] = GR{
    prs => import prs._
    CartDetailsRow.tupled((<<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table cart_details. Objects of this class serve as prototypes for rows in queries. */
  class CartDetails(_tableTag: Tag) extends profile.api.Table[CartDetailsRow](_tableTag, Some("myshop2"), "cart_details") {
    def * = (cartDetailId, cartId, optionItemId) <> (CartDetailsRow.tupled, CartDetailsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cartDetailId), Rep.Some(cartId), Rep.Some(optionItemId))).shaped.<>({r=>import r._; _1.map(_=> CartDetailsRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column cart_detail_id SqlType(INT), AutoInc, PrimaryKey */
    val cartDetailId: Rep[Int] = column[Int]("cart_detail_id", O.AutoInc, O.PrimaryKey)
    /** Database column cart_id SqlType(INT) */
    val cartId: Rep[Int] = column[Int]("cart_id")
    /** Database column option_item_id SqlType(INT) */
    val optionItemId: Rep[Int] = column[Int]("option_item_id")

    /** Foreign key referencing Carts (database name cart_details_ibfk_1) */
    lazy val cartsFk = foreignKey("cart_details_ibfk_1", cartId, Carts)(r => r.cartId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table CartDetails */
  lazy val CartDetails = new TableQuery(tag => new CartDetails(tag))

  /** Entity class storing rows of table Carts
   *  @param userId Database column user_id SqlType(VARCHAR), Length(20,true)
   *  @param cartId Database column cart_id SqlType(INT), AutoInc, PrimaryKey
   *  @param productId Database column product_id SqlType(INT)
   *  @param quantity Database column quantity SqlType(INT)
   *  @param addedDate Database column added_date SqlType(DATETIME) */
  case class CartsRow(userId: String, cartId: Int, productId: Int, quantity: Int, addedDate: java.sql.Timestamp)
  /** GetResult implicit for fetching CartsRow objects using plain SQL queries */
  implicit def GetResultCartsRow(implicit e0: GR[String], e1: GR[Int], e2: GR[java.sql.Timestamp]): GR[CartsRow] = GR{
    prs => import prs._
    CartsRow.tupled((<<[String], <<[Int], <<[Int], <<[Int], <<[java.sql.Timestamp]))
  }
  /** Table description of table carts. Objects of this class serve as prototypes for rows in queries. */
  class Carts(_tableTag: Tag) extends profile.api.Table[CartsRow](_tableTag, Some("myshop2"), "carts") {
    def * = (userId, cartId, productId, quantity, addedDate) <> (CartsRow.tupled, CartsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(cartId), Rep.Some(productId), Rep.Some(quantity), Rep.Some(addedDate))).shaped.<>({r=>import r._; _1.map(_=> CartsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(VARCHAR), Length(20,true) */
    val userId: Rep[String] = column[String]("user_id", O.Length(20,varying=true))
    /** Database column cart_id SqlType(INT), AutoInc, PrimaryKey */
    val cartId: Rep[Int] = column[Int]("cart_id", O.AutoInc, O.PrimaryKey)
    /** Database column product_id SqlType(INT) */
    val productId: Rep[Int] = column[Int]("product_id")
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

  /** Entity class storing rows of table NonUserCartDetails
   *  @param nonUserCartId Database column non_user_cart_id SqlType(INT)
   *  @param nonUserCartDetailId Database column non_user_cart_detail_id SqlType(INT UNSIGNED), AutoInc, PrimaryKey
   *  @param optionItemId Database column option_item_id SqlType(INT UNSIGNED) */
  case class NonUserCartDetailsRow(nonUserCartId: Int, nonUserCartDetailId: Long, optionItemId: Long)
  /** GetResult implicit for fetching NonUserCartDetailsRow objects using plain SQL queries */
  implicit def GetResultNonUserCartDetailsRow(implicit e0: GR[Int], e1: GR[Long]): GR[NonUserCartDetailsRow] = GR{
    prs => import prs._
    NonUserCartDetailsRow.tupled((<<[Int], <<[Long], <<[Long]))
  }
  /** Table description of table non_user_cart_details. Objects of this class serve as prototypes for rows in queries. */
  class NonUserCartDetails(_tableTag: Tag) extends profile.api.Table[NonUserCartDetailsRow](_tableTag, Some("myshop2"), "non_user_cart_details") {
    def * = (nonUserCartId, nonUserCartDetailId, optionItemId) <> (NonUserCartDetailsRow.tupled, NonUserCartDetailsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(nonUserCartId), Rep.Some(nonUserCartDetailId), Rep.Some(optionItemId))).shaped.<>({r=>import r._; _1.map(_=> NonUserCartDetailsRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column non_user_cart_id SqlType(INT) */
    val nonUserCartId: Rep[Int] = column[Int]("non_user_cart_id")
    /** Database column non_user_cart_detail_id SqlType(INT UNSIGNED), AutoInc, PrimaryKey */
    val nonUserCartDetailId: Rep[Long] = column[Long]("non_user_cart_detail_id", O.AutoInc, O.PrimaryKey)
    /** Database column option_item_id SqlType(INT UNSIGNED) */
    val optionItemId: Rep[Long] = column[Long]("option_item_id")

    /** Foreign key referencing NonUserCarts (database name FK__non_user_carts) */
    lazy val nonUserCartsFk = foreignKey("FK__non_user_carts", nonUserCartId, NonUserCarts)(r => r.nonUserCartId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table NonUserCartDetails */
  lazy val NonUserCartDetails = new TableQuery(tag => new NonUserCartDetails(tag))

  /** Entity class storing rows of table NonUserCarts
   *  @param idToken Database column id_token SqlType(VARCHAR), Length(100,true)
   *  @param nonUserCartId Database column non_user_cart_id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(100,true)
   *  @param productId Database column product_id SqlType(INT)
   *  @param price Database column price SqlType(INT)
   *  @param quantity Database column quantity SqlType(INT)
   *  @param addedDate Database column added_date SqlType(DATETIME) */
  case class NonUserCartsRow(idToken: String, nonUserCartId: Int, name: String, productId: Int, price: Int, quantity: Int, addedDate: java.sql.Timestamp)
  /** GetResult implicit for fetching NonUserCartsRow objects using plain SQL queries */
  implicit def GetResultNonUserCartsRow(implicit e0: GR[String], e1: GR[Int], e2: GR[java.sql.Timestamp]): GR[NonUserCartsRow] = GR{
    prs => import prs._
    NonUserCartsRow.tupled((<<[String], <<[Int], <<[String], <<[Int], <<[Int], <<[Int], <<[java.sql.Timestamp]))
  }
  /** Table description of table non_user_carts. Objects of this class serve as prototypes for rows in queries. */
  class NonUserCarts(_tableTag: Tag) extends profile.api.Table[NonUserCartsRow](_tableTag, Some("myshop2"), "non_user_carts") {
    def * = (idToken, nonUserCartId, name, productId, price, quantity, addedDate) <> (NonUserCartsRow.tupled, NonUserCartsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(idToken), Rep.Some(nonUserCartId), Rep.Some(name), Rep.Some(productId), Rep.Some(price), Rep.Some(quantity), Rep.Some(addedDate))).shaped.<>({r=>import r._; _1.map(_=> NonUserCartsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id_token SqlType(VARCHAR), Length(100,true) */
    val idToken: Rep[String] = column[String]("id_token", O.Length(100,varying=true))
    /** Database column non_user_cart_id SqlType(INT), AutoInc, PrimaryKey */
    val nonUserCartId: Rep[Int] = column[Int]("non_user_cart_id", O.AutoInc, O.PrimaryKey)
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

    /** Foreign key referencing Products (database name non_user_carts_ibfk_1) */
    lazy val productsFk = foreignKey("non_user_carts_ibfk_1", productId, Products)(r => r.productId, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)

    /** Index over (idToken) (database name user_id) */
    val index1 = index("user_id", idToken)
  }
  /** Collection-like TableQuery object for table NonUserCarts */
  lazy val NonUserCarts = new TableQuery(tag => new NonUserCarts(tag))

  /** Entity class storing rows of table OrderProductDetails
   *  @param orderProductId Database column order_product_id SqlType(INT)
   *  @param orderProductDetailId Database column order_product_detail_id SqlType(INT), AutoInc, PrimaryKey
   *  @param optionItemId Database column option_item_id SqlType(INT) */
  case class OrderProductDetailsRow(orderProductId: Int, orderProductDetailId: Int, optionItemId: Int)
  /** GetResult implicit for fetching OrderProductDetailsRow objects using plain SQL queries */
  implicit def GetResultOrderProductDetailsRow(implicit e0: GR[Int]): GR[OrderProductDetailsRow] = GR{
    prs => import prs._
    OrderProductDetailsRow.tupled((<<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table order_product_details. Objects of this class serve as prototypes for rows in queries. */
  class OrderProductDetails(_tableTag: Tag) extends profile.api.Table[OrderProductDetailsRow](_tableTag, Some("myshop2"), "order_product_details") {
    def * = (orderProductId, orderProductDetailId, optionItemId) <> (OrderProductDetailsRow.tupled, OrderProductDetailsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(orderProductId), Rep.Some(orderProductDetailId), Rep.Some(optionItemId))).shaped.<>({r=>import r._; _1.map(_=> OrderProductDetailsRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column order_product_id SqlType(INT) */
    val orderProductId: Rep[Int] = column[Int]("order_product_id")
    /** Database column order_product_detail_id SqlType(INT), AutoInc, PrimaryKey */
    val orderProductDetailId: Rep[Int] = column[Int]("order_product_detail_id", O.AutoInc, O.PrimaryKey)
    /** Database column option_item_id SqlType(INT) */
    val optionItemId: Rep[Int] = column[Int]("option_item_id")

    /** Foreign key referencing OrderProducts (database name FK__order_products) */
    lazy val orderProductsFk = foreignKey("FK__order_products", orderProductId, OrderProducts)(r => r.orderProductId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing ProductOptionItems (database name FK__product_option_items) */
    lazy val productOptionItemsFk = foreignKey("FK__product_option_items", optionItemId, ProductOptionItems)(r => r.productOptionItemId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table OrderProductDetails */
  lazy val OrderProductDetails = new TableQuery(tag => new OrderProductDetails(tag))

  /** Entity class storing rows of table OrderProducts
   *  @param orderId Database column order_id SqlType(INT)
   *  @param orderProductId Database column order_product_id SqlType(INT), AutoInc, PrimaryKey
   *  @param productId Database column product_id SqlType(INT)
   *  @param sellerId Database column seller_id SqlType(VARCHAR), Length(50,true), Default()
   *  @param quantity Database column quantity SqlType(INT), Default(0)
   *  @param address Database column address SqlType(VARCHAR), Length(50,true), Default(0)
   *  @param state Database column state SqlType(INT), Default(0) */
  case class OrderProductsRow(orderId: Int, orderProductId: Int, productId: Int, sellerId: String = "", quantity: Int = 0, address: String = "0", state: Int = 0)
  /** GetResult implicit for fetching OrderProductsRow objects using plain SQL queries */
  implicit def GetResultOrderProductsRow(implicit e0: GR[Int], e1: GR[String]): GR[OrderProductsRow] = GR{
    prs => import prs._
    OrderProductsRow.tupled((<<[Int], <<[Int], <<[Int], <<[String], <<[Int], <<[String], <<[Int]))
  }
  /** Table description of table order_products. Objects of this class serve as prototypes for rows in queries. */
  class OrderProducts(_tableTag: Tag) extends profile.api.Table[OrderProductsRow](_tableTag, Some("myshop2"), "order_products") {
    def * = (orderId, orderProductId, productId, sellerId, quantity, address, state) <> (OrderProductsRow.tupled, OrderProductsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(orderId), Rep.Some(orderProductId), Rep.Some(productId), Rep.Some(sellerId), Rep.Some(quantity), Rep.Some(address), Rep.Some(state))).shaped.<>({r=>import r._; _1.map(_=> OrderProductsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column order_id SqlType(INT) */
    val orderId: Rep[Int] = column[Int]("order_id")
    /** Database column order_product_id SqlType(INT), AutoInc, PrimaryKey */
    val orderProductId: Rep[Int] = column[Int]("order_product_id", O.AutoInc, O.PrimaryKey)
    /** Database column product_id SqlType(INT) */
    val productId: Rep[Int] = column[Int]("product_id")
    /** Database column seller_id SqlType(VARCHAR), Length(50,true), Default() */
    val sellerId: Rep[String] = column[String]("seller_id", O.Length(50,varying=true), O.Default(""))
    /** Database column quantity SqlType(INT), Default(0) */
    val quantity: Rep[Int] = column[Int]("quantity", O.Default(0))
    /** Database column address SqlType(VARCHAR), Length(50,true), Default(0) */
    val address: Rep[String] = column[String]("address", O.Length(50,varying=true), O.Default("0"))
    /** Database column state SqlType(INT), Default(0) */
    val state: Rep[Int] = column[Int]("state", O.Default(0))

    /** Foreign key referencing Orders (database name FK__orders) */
    lazy val ordersFk = foreignKey("FK__orders", orderId, Orders)(r => r.orderId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Products (database name FK__products) */
    lazy val productsFk = foreignKey("FK__products", productId, Products)(r => r.productId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Sellers (database name FK__sellers) */
    lazy val sellersFk = foreignKey("FK__sellers", sellerId, Sellers)(r => r.sellerId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table OrderProducts */
  lazy val OrderProducts = new TableQuery(tag => new OrderProducts(tag))

  /** Entity class storing rows of table Orders
   *  @param orderId Database column order_id SqlType(INT), AutoInc, PrimaryKey
   *  @param userId Database column user_id SqlType(VARCHAR), Length(100,true)
   *  @param orderDate Database column order_date SqlType(TIMESTAMP) */
  case class OrdersRow(orderId: Int, userId: String, orderDate: java.sql.Timestamp)
  /** GetResult implicit for fetching OrdersRow objects using plain SQL queries */
  implicit def GetResultOrdersRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[OrdersRow] = GR{
    prs => import prs._
    OrdersRow.tupled((<<[Int], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table orders. Objects of this class serve as prototypes for rows in queries. */
  class Orders(_tableTag: Tag) extends profile.api.Table[OrdersRow](_tableTag, Some("myshop2"), "orders") {
    def * = (orderId, userId, orderDate) <> (OrdersRow.tupled, OrdersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(orderId), Rep.Some(userId), Rep.Some(orderDate))).shaped.<>({r=>import r._; _1.map(_=> OrdersRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column order_id SqlType(INT), AutoInc, PrimaryKey */
    val orderId: Rep[Int] = column[Int]("order_id", O.AutoInc, O.PrimaryKey)
    /** Database column user_id SqlType(VARCHAR), Length(100,true) */
    val userId: Rep[String] = column[String]("user_id", O.Length(100,varying=true))
    /** Database column order_date SqlType(TIMESTAMP) */
    val orderDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("order_date")

    /** Foreign key referencing Users (database name FK__users) */
    lazy val usersFk = foreignKey("FK__users", userId, Users)(r => r.userId, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
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
   *  @param surcharge Database column surcharge SqlType(INT) */
  case class ProductOptionItemsRow(productOptionId: Int, productOptionItemId: Int, name: String, itemSequence: Int, surcharge: Int)
  /** GetResult implicit for fetching ProductOptionItemsRow objects using plain SQL queries */
  implicit def GetResultProductOptionItemsRow(implicit e0: GR[Int], e1: GR[String]): GR[ProductOptionItemsRow] = GR{
    prs => import prs._
    ProductOptionItemsRow.tupled((<<[Int], <<[Int], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table product_option_items. Objects of this class serve as prototypes for rows in queries. */
  class ProductOptionItems(_tableTag: Tag) extends profile.api.Table[ProductOptionItemsRow](_tableTag, Some("myshop2"), "product_option_items") {
    def * = (productOptionId, productOptionItemId, name, itemSequence, surcharge) <> (ProductOptionItemsRow.tupled, ProductOptionItemsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(productOptionId), Rep.Some(productOptionItemId), Rep.Some(name), Rep.Some(itemSequence), Rep.Some(surcharge))).shaped.<>({r=>import r._; _1.map(_=> ProductOptionItemsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
   *  @param productId Database column product_id SqlType(INT)
   *  @param stock Database column stock SqlType(INT UNSIGNED)
   *  @param productStockId Database column product_stock_id SqlType(INT), AutoInc, PrimaryKey
   *  @param parentId Database column parent_id SqlType(INT)
   *  @param productOptionItemId Database column product_option_item_id SqlType(INT)
   *  @param depth Database column depth SqlType(INT) */
  case class ProductStockRow(productId: Int, stock: Long, productStockId: Int, parentId: Int, productOptionItemId: Int, depth: Int)
  /** GetResult implicit for fetching ProductStockRow objects using plain SQL queries */
  implicit def GetResultProductStockRow(implicit e0: GR[Int], e1: GR[Long]): GR[ProductStockRow] = GR{
    prs => import prs._
    ProductStockRow.tupled((<<[Int], <<[Long], <<[Int], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table product_stock. Objects of this class serve as prototypes for rows in queries. */
  class ProductStock(_tableTag: Tag) extends profile.api.Table[ProductStockRow](_tableTag, Some("myshop2"), "product_stock") {
    def * = (productId, stock, productStockId, parentId, productOptionItemId, depth) <> (ProductStockRow.tupled, ProductStockRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(productId), Rep.Some(stock), Rep.Some(productStockId), Rep.Some(parentId), Rep.Some(productOptionItemId), Rep.Some(depth))).shaped.<>({r=>import r._; _1.map(_=> ProductStockRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column product_id SqlType(INT) */
    val productId: Rep[Int] = column[Int]("product_id")
    /** Database column stock SqlType(INT UNSIGNED) */
    val stock: Rep[Long] = column[Long]("stock")
    /** Database column product_stock_id SqlType(INT), AutoInc, PrimaryKey */
    val productStockId: Rep[Int] = column[Int]("product_stock_id", O.AutoInc, O.PrimaryKey)
    /** Database column parent_id SqlType(INT) */
    val parentId: Rep[Int] = column[Int]("parent_id")
    /** Database column product_option_item_id SqlType(INT) */
    val productOptionItemId: Rep[Int] = column[Int]("product_option_item_id")
    /** Database column depth SqlType(INT) */
    val depth: Rep[Int] = column[Int]("depth")

    /** Foreign key referencing ProductOptionItems (database name FK_product_stock_product_option_items) */
    lazy val productOptionItemsFk = foreignKey("FK_product_stock_product_option_items", productOptionItemId, ProductOptionItems)(r => r.productOptionItemId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Products (database name FK_product_stock_products) */
    lazy val productsFk = foreignKey("FK_product_stock_products", productId, Products)(r => r.productId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
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

  /** Entity class storing rows of table ReviewImages
   *  @param reviewId Database column review_id SqlType(INT)
   *  @param reviewImageId Database column review_image_id SqlType(INT), PrimaryKey
   *  @param image Database column image SqlType(LONGTEXT), Length(2147483647,true)
   *  @param sequence Database column sequence SqlType(INT) */
  case class ReviewImagesRow(reviewId: Int, reviewImageId: Int, image: String, sequence: Int)
  /** GetResult implicit for fetching ReviewImagesRow objects using plain SQL queries */
  implicit def GetResultReviewImagesRow(implicit e0: GR[Int], e1: GR[String]): GR[ReviewImagesRow] = GR{
    prs => import prs._
    ReviewImagesRow.tupled((<<[Int], <<[Int], <<[String], <<[Int]))
  }
  /** Table description of table review_images. Objects of this class serve as prototypes for rows in queries. */
  class ReviewImages(_tableTag: Tag) extends profile.api.Table[ReviewImagesRow](_tableTag, Some("myshop2"), "review_images") {
    def * = (reviewId, reviewImageId, image, sequence) <> (ReviewImagesRow.tupled, ReviewImagesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(reviewId), Rep.Some(reviewImageId), Rep.Some(image), Rep.Some(sequence))).shaped.<>({r=>import r._; _1.map(_=> ReviewImagesRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column review_id SqlType(INT) */
    val reviewId: Rep[Int] = column[Int]("review_id")
    /** Database column review_image_id SqlType(INT), PrimaryKey */
    val reviewImageId: Rep[Int] = column[Int]("review_image_id", O.PrimaryKey)
    /** Database column image SqlType(LONGTEXT), Length(2147483647,true) */
    val image: Rep[String] = column[String]("image", O.Length(2147483647,varying=true))
    /** Database column sequence SqlType(INT) */
    val sequence: Rep[Int] = column[Int]("sequence")

    /** Foreign key referencing Reviews (database name FK__reviews) */
    lazy val reviewsFk = foreignKey("FK__reviews", reviewId, Reviews)(r => r.reviewId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table ReviewImages */
  lazy val ReviewImages = new TableQuery(tag => new ReviewImages(tag))

  /** Entity class storing rows of table Reviews
   *  @param productId Database column product_id SqlType(INT)
   *  @param reviewId Database column review_id SqlType(INT), AutoInc, PrimaryKey
   *  @param userId Database column user_id SqlType(VARCHAR), Length(20,true)
   *  @param rating Database column rating SqlType(SMALLINT)
   *  @param title Database column title SqlType(VARCHAR), Length(200,true)
   *  @param content Database column content SqlType(VARCHAR), Length(2000,true)
   *  @param comment Database column comment SqlType(VARCHAR), Length(500,true)
   *  @param reviewDate Database column review_date SqlType(TIMESTAMP)
   *  @param recommend Database column recommend SqlType(INT)
   *  @param orderProductId Database column order_product_id SqlType(INT) */
  case class ReviewsRow(productId: Int, reviewId: Int, userId: String, rating: Int, title: String, content: String, comment: String, reviewDate: java.sql.Timestamp, recommend: Int, orderProductId: Int)
  /** GetResult implicit for fetching ReviewsRow objects using plain SQL queries */
  implicit def GetResultReviewsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[ReviewsRow] = GR{
    prs => import prs._
    ReviewsRow.tupled((<<[Int], <<[Int], <<[String], <<[Int], <<[String], <<[String], <<[String], <<[java.sql.Timestamp], <<[Int], <<[Int]))
  }
  /** Table description of table reviews. Objects of this class serve as prototypes for rows in queries. */
  class Reviews(_tableTag: Tag) extends profile.api.Table[ReviewsRow](_tableTag, Some("myshop2"), "reviews") {
    def * = (productId, reviewId, userId, rating, title, content, comment, reviewDate, recommend, orderProductId) <> (ReviewsRow.tupled, ReviewsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(productId), Rep.Some(reviewId), Rep.Some(userId), Rep.Some(rating), Rep.Some(title), Rep.Some(content), Rep.Some(comment), Rep.Some(reviewDate), Rep.Some(recommend), Rep.Some(orderProductId))).shaped.<>({r=>import r._; _1.map(_=> ReviewsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column product_id SqlType(INT) */
    val productId: Rep[Int] = column[Int]("product_id")
    /** Database column review_id SqlType(INT), AutoInc, PrimaryKey */
    val reviewId: Rep[Int] = column[Int]("review_id", O.AutoInc, O.PrimaryKey)
    /** Database column user_id SqlType(VARCHAR), Length(20,true) */
    val userId: Rep[String] = column[String]("user_id", O.Length(20,varying=true))
    /** Database column rating SqlType(SMALLINT) */
    val rating: Rep[Int] = column[Int]("rating")
    /** Database column title SqlType(VARCHAR), Length(200,true) */
    val title: Rep[String] = column[String]("title", O.Length(200,varying=true))
    /** Database column content SqlType(VARCHAR), Length(2000,true) */
    val content: Rep[String] = column[String]("content", O.Length(2000,varying=true))
    /** Database column comment SqlType(VARCHAR), Length(500,true) */
    val comment: Rep[String] = column[String]("comment", O.Length(500,varying=true))
    /** Database column review_date SqlType(TIMESTAMP) */
    val reviewDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("review_date")
    /** Database column recommend SqlType(INT) */
    val recommend: Rep[Int] = column[Int]("recommend")
    /** Database column order_product_id SqlType(INT) */
    val orderProductId: Rep[Int] = column[Int]("order_product_id")

    /** Foreign key referencing OrderProducts (database name FK_reviews_order_products) */
    lazy val orderProductsFk = foreignKey("FK_reviews_order_products", orderProductId, OrderProducts)(r => r.orderProductId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Products (database name reviews_ibfk_2) */
    lazy val productsFk = foreignKey("reviews_ibfk_2", productId, Products)(r => r.productId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Users (database name reviews_ibfk_1) */
    lazy val usersFk = foreignKey("reviews_ibfk_1", userId, Users)(r => r.userId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
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
