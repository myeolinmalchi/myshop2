package dto

import models.Tables._

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
	def newEntity(user: Users#TableElementType) =
		new UserDto(user.userId, user.userPw, user.name, user.email, user.phonenumber)
		
	def apply(userId: String,
			  userPw: String,
			  name: String,
			  email: String,
			  phonenumber: String) =
		new UserDto(userId, userPw, name, email, phonenumber)
}

object AddressDto{
	def apply(addr: UserAddresses#TableElementType) =
		new AddressDto(addr.userId, addr.addressId, addr.priority, addr.address, addr.addressDetail, addr.zipcode)
}