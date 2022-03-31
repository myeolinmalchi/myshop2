package models

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import slick.jdbc.MySQLProfile.api._
import slick.lifted.AbstractTable

// TODO
//  - db insertion 구현
@Singleton
class CommonModelApi(db: Database)(implicit ec: ExecutionContext) {
	
	def select[T<:AbstractTable[_], D](g: T#TableElementType => D)
									  (f: T => Rep[Boolean])
									  (implicit query :TableQuery[T]): Future[List[D]] =
		db run query.filter(f(_)).result map (_ map { row => g(row) } toList )
	
	def selectOne[T<:AbstractTable[_],D](g: T#TableElementType => D)
										(f: T => Rep[Boolean])
										(implicit query: TableQuery[T]): Future[Option[D]] =
		db run query.filter(f).result map (_.headOption match {
			case Some(value) => Some(g(value))
			case None => None
		})
		
}