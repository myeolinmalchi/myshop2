package dto

import models.Tables._
import play.api.libs.json.{Json, Reads, Writes}
import scala.collection.mutable.Map
import scala.language.implicitConversions

case class UserDto(userId: String,
				   userPw: String,
				   name: String,
				   email: String,
				   phonenumber: String)

case class UserRequestDto(userId: Option[String] = None,
						  userPw: Option[String] = None,
						  name: Option[String] = None,
						  email: Option[String] = None,
						  phonenumber: Option[String] = None)

case class AddressDto(userId: String,
					  addressId: Int,
					  priority: Int,
					  address: String,
					  addressDetail: String,
					  zipcode: Int)

object UserDto {
	implicit class RowToDto(row: UsersRow) {
		def toDto: UserDto =
			UserDto (
				userId = row.userId,
				userPw = row.userPw,
				name = row.name,
				email = row.email,
				phonenumber = row.phonenumber
			)
	}
	
	implicit def writes: Writes[UserDto] = Json.writes[UserDto]
	implicit def reads: Reads[UserDto] = Json.reads[UserDto]
}

object UserRequestDto{
	
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
	
	val NONE_MATCH_MSG: Map[String, String] = Map (
		USER_ID -> "유효하지 않은 아이디입니다.",
		USER_PW -> "유효하지 않은 비밀번호입니다.",
		NAME -> "유효하지 않은 이름입니다.",
		EMAIL -> "유효하지 않은 이메일입니다.",
		PHONE -> "유효하지 않은 전화번호입니다.",
	)
	
	val OVERLAP_EXCEPTION: Map[String, Exception] = Map (
		USER_ID -> new IllegalArgumentException("이미 존재하는 계정입니다."),
		EMAIL -> new IllegalArgumentException("이미 존재하는 이메일입니다.")
	)
	
	val OVERLAP_MSG: Map[String, String] = Map (
		USER_ID -> "이미 존재하는 계정입니다.",
		EMAIL -> "이미 존재하는 이메일입니다."
	)
	
	def empty: UserRequestDto = UserRequestDto()
	
	implicit def writes: Writes[UserRequestDto] = Json.writes[UserRequestDto]
	implicit def reads: Reads[UserRequestDto] = Json.reads[UserRequestDto]
}

object AddressDto{

}