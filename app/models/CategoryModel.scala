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
import play.api.libs.json.Json
import scala.language.postfixOps
import slick.lifted.AbstractTable

// TODO
//  - db insertion 구현
@Singleton
class CategoryModel(db: Database)(implicit ec: ExecutionContext) {
	
	def getMainCategories =
		db run sql"SELECT * FROM v_categories WHERE depth=0".as[(String, String, Int, Int)]
	
	def getChildrens(code: String) = ???
	
	def getSiblings(code: String) = ???
}
