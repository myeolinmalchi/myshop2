package services.user

import dto.UserDto
import dto.UserDto.{EMAIL, NAME, NONE_MATCH_EXCEPTION, OVERLAP_EXCEPTION, PATTERNS, PHONE, USER_ID, USER_PW}
import models.UserModel
import scala.concurrent.{ExecutionContext, Future}

object Common {
	
	def accountValidation(user: UserDto)
						 (implicit userModel: UserModel, ec: ExecutionContext): Future[UserDto] = {
		import UserDto._
		
		def checkPattern(key: String)(implicit str: String): Future[String] =
			if (!str.matches(PATTERNS(key))) Future.failed(NONE_MATCH_EXCEPTION(key))
			else Future.successful(str)
		
		def validUserId(implicit userId: String): Future[String] =
			for {
				patternCheck <- checkPattern(USER_ID)
				existenceCheck <- userModel checkUserExist patternCheck
			} yield existenceCheck match {
				case None => userId
				case Some(_) => throw OVERLAP_EXCEPTION(USER_ID)
			}
		
		def validUserPw(implicit userPw: String): Future[String] = checkPattern(USER_PW)
		
		def validName(implicit name: String): Future[String] = checkPattern(NAME)
		
		def validEmail(implicit email: String): Future[String] =
			for {
				patternCheck <-checkPattern(EMAIL)
				existenceCheck <- userModel checkEmailExist patternCheck
			} yield existenceCheck match {
				case None => email
				case Some(_) => throw OVERLAP_EXCEPTION(EMAIL)
			}
		
		def validPhone(implicit phone: String): Future[String] = checkPattern(PHONE)
		
		for {
			userId <- validUserId(user.userId.get)
			userPw <- validUserPw(user.userPw.get)
			name <- validName(user.name.get)
			email <- validEmail(user.email.get)
			phone <- validPhone(user.phonenumber.get)
		} yield user
	}
	
	
}
