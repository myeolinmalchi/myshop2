package models

import javax.inject._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

// 카테고리에 대한 정보를 받아오는 클래스
// categories 테이블이 아닌 v_categories 뷰에 접근한다.

// TODO
//  - db insertion 구현
@Singleton
class CategoryModel @Inject() (val dbConfigProvider: DatabaseConfigProvider)
							  (implicit ec: ExecutionContext)
	extends HasDatabaseConfigProvider[JdbcProfile]{
	
	def getMainCategories =
		db run sql"""
				SELECT code, name
				FROM v_categories
				WHERE depth=0
				""".as[(String, String)] map (_.toList)
	
	def getChildrens(code: String) = {
		val query =
			sql"""
				SELECT a.code, a.name
				FROM v_categories a INNER JOIN (SELECT * FROM v_categories WHERE code=${code}) b
				WHERE a.code LIKE concat(b.code, '%') AND a.depth = b.depth+1
				"""
		db run query.as[(String, String)] map (_.toList)
	}
	
	def getSiblings(code: String) = {
		val query =
			sql"""
				SELECT a.code, a.name
				FROM v_categories a INNER JOIN (SELECT * FROM v_categories WHERE code=${code}) b
				WHERE substr(a.code,1,1) = substr(b.code,1,1) AND a.depth = b.depth
			   """
		db run query.as[(String, String)] map (_.toList)
	}
}
