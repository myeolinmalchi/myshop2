package models

import scala.concurrent.Future

trait AbstractModel[O, P] {
	
	def insert(dto: O): Future[Int]
	def select(primaryKey: P): Future[Option[O]]
	def update(dto: O): Future[Int]
	def delete(primaryKey: P): Future[Option[O]]
	
}
