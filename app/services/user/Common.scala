package services.user

import dto.UserDto
import dto.UserDto.{EMAIL, NAME, NONE_MATCH_EXCEPTION, OVERLAP_EXCEPTION, PATTERNS, PHONE, USER_ID, USER_PW}
import models.UserModel
import scala.concurrent.{ExecutionContext, Future}

object Common {
	
	import UserDto._
	
	def checkPattern(key: String)(implicit str: String): Future[String] =
		if (!str.matches(PATTERNS(key))) Future.failed(NONE_MATCH_EXCEPTION(key))
		else Future.successful(str)
	
	def validUserId(userId: String)
				   (implicit userModel: UserModel, ec: ExecutionContext): Future[String] =
		for {
			patternCheck <- checkPattern(USER_ID)(userId)
			existenceCheck <- userModel checkUserExist patternCheck
		} yield existenceCheck match {
			case None => userId
			case Some(_) => throw OVERLAP_EXCEPTION(USER_ID)
		}
	
	def validUserPw(userPw: String): Future[String] = checkPattern(USER_PW)(userPw)
	
	def validName(name: String): Future[String] = checkPattern(NAME)(name)
	
	def validEmail(email: String)
				  (implicit userModel: UserModel,
				   ec: ExecutionContext): Future[String] =
		for {
			patternCheck <-checkPattern(EMAIL)(email)
			existenceCheck <- userModel checkEmailExist patternCheck
		} yield existenceCheck match {
			case None => email
			case Some(_) => throw OVERLAP_EXCEPTION(EMAIL)
		}
	
	def validPhone(implicit phone: String): Future[String] = checkPattern(PHONE)
	
	
	def accountValidation(user: UserDto)
						 (implicit userModel: UserModel, ec: ExecutionContext): Future[UserDto] = {
		for {
			userId <- validUserId(user.userId.get)
			userPw <- validUserPw(user.userPw.get)
			name <- validName(user.name.get)
			email <- validEmail(user.email.get)
			phone <- validPhone(user.phonenumber.get)
		} yield user
	}
	
	def kakaoAccountValidation(user: UserDto)
							  (implicit userModel: UserModel, ec: ExecutionContext): Future[UserDto] = {
		for {
			name <- validName(user.name.get)
			phone <- validPhone(user.phonenumber.get)
		} yield user
	}
}
