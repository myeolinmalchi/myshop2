package models

import scala.concurrent.{ExecutionContext, Future}
import slick.lifted.AbstractTable
import scala.concurrent.ExecutionContext
import dto._
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api._
import models.Tables._
import scala.collection.mutable.Map
import scala.util.{Failure, Success, Try}
import java.security.MessageDigest
import java.math.BigInteger
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import common.encryption._
import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import javax.imageio.ImageIO
import play.api.libs.json.Json
import scala.language.postfixOps
import slick.lifted.AbstractTable
import org.apache.commons.codec.binary.Base64

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
		
	def insertWithId = ???
}