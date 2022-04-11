package dto

import models.Tables._
import play.api.libs.json.Json
import scala.collection.mutable.Map

case class UserDto(userId: String,
				   userPw: String,
				   name: String,
				   email: String,
				   phonenumber: String)

case class AddressDto(userId: String,
					  addressId: Int,
					  priority: Int,
					  address: String,
					  addressDetail: String,
					  zipcode: Int)

object UserDto{
	
	implicit val userWrites = Json.writes[UserDto]
	implicit val userReads = Json.reads[UserDto]
	
	val USER_ID = "userId"
	val USER_PW = "userPw"
	val NAME = "name"
	val EMAIL = "email"
	val PHONE = "phonenumber"
	
	val PATTERNS: Map[String, String] = Map (
		USER_ID -> "^[a-z]+[a-z0-9]{5,19}$",
		USER_PW -> "^(?=.*\\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,16}$",
		NAME -> "^[ㄱ-힣]+$",
		EMAIL -> "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$",
		PHONE -> "^\\d{3}-\\d{3,4}-\\d{4}$"
	)
	
	val NONE_MATCH_EXCEPTION: Map[String, Exception] = Map (
		USER_ID -> new IllegalArgumentException("유효하지 않은 아이디입니다."),
		USER_PW -> new IllegalArgumentException("유효하지 않은 비밀번호입니다."),
		NAME -> new IllegalArgumentException("유효하지 않은 이름입니다."),
		EMAIL -> new IllegalArgumentException("유효하지 않은 이메일입니다."),
		PHONE -> new IllegalArgumentException("유효하지 않은 전화번호입니다."),
	)
	
	val OVERLAP_EXCEPTION: Map[String, Exception] = Map (
		USER_ID -> new IllegalArgumentException("이미 존재하는 계정입니다."),
		EMAIL -> new IllegalArgumentException("이미 존재하는 이메일입니다.")
	)
	
	def newEntity(user: Users#TableElementType) =
		new UserDto(user.userId, user.userPw, user.name, user.email, user.phonenumber)
		
	def apply(userId: String,
			  userPw: String,
			  name: String,
			  email: String,
			  phonenumber: String) =
		new UserDto(userId, userPw, name, email, phonenumber)
		
	def empty = new UserDto("", "", "", "", "")
}

object AddressDto{
	def apply(addr: UserAddresses#TableElementType) =
		new AddressDto(addr.userId, addr.addressId, addr.priority, addr.address, addr.addressDetail, addr.zipcode)
}