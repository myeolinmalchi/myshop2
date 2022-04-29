package services.user

import cats.data.{EitherT, OptionT}
import common.validation.ValidationResultLib
import dto.UserDto
import dto.UserDto.{EMAIL, NAME, NONE_MATCH_MSG, OVERLAP_MSG, PATTERNS, PHONE, USER_ID, USER_PW}
import javax.inject.{Inject, Singleton}
import models.UserModel
import play.api.libs.ws.WSClient
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import services.user.Common._

@Singleton
class AccountServiceImpl @Inject() ()(implicit ws: WSClient, ec: ExecutionContext, userModel: UserModel)
		extends AccountService with ValidationResultLib[Future]{
	
	implicit class UserValidator(user: UserDto) {
		private def checkPattern(str: String, key: String): ValidationResult[ValidationFailure, Unit] =
			ValidationResult.ensure(
				str.matches(PATTERNS(key)),
				onFailure = ValidationFailure(NONE_MATCH_MSG(key))
			)
		
		private def validUserId(userId: String): ValidationResult[ValidationFailure, Unit] =
			for {
				_ <- checkPattern(userId, USER_ID)
				_ <- ValidationResult.ensureM(
					userModel userIdDoesNotExist userId,
					onFailure = ValidationFailure(OVERLAP_MSG(USER_ID))
				)
			} yield ()
		
		private def validUserPw(userPw: String): ValidationResult[ValidationFailure, Unit] =
			checkPattern(userPw, USER_PW)
		
		private def validName(name: String): ValidationResult[ValidationFailure, Unit] =
			checkPattern(name, NAME)
		
		private def validEmail(email: String): ValidationResult[ValidationFailure, Unit] =
			for {
				_ <- checkPattern(email, EMAIL)
				_ <- ValidationResult.ensureM(
					userModel emailDoesNotExist email,
					onFailure = ValidationFailure(OVERLAP_MSG(EMAIL))
				)
			} yield ()
		
		private def validPhone(phone: String): ValidationResult[ValidationFailure, Unit] =
			checkPattern(phone, PHONE)
		
		def accountValidation: Option[ValidationResult[ValidationFailure, Unit]] =
			 for {
				 userId <- user.userId
				 userPw <- user.userPw
				 name <- user.name
				 email <- user.email
				 phone <- user.phonenumber
			 } yield for {
				 _ <- validUserId(userId)
				 _ <- validUserPw(userPw)
				 _ <- validName(name)
				 _ <- validEmail(email)
				 _ <- validPhone(phone)
			 } yield ()
	
		def kakaoAccountValidation: Option[ValidationResult[ValidationFailure, Unit]] =
			for {
				name <- user.name
				phone <- user.phonenumber
			} yield for {
				_ <- validName(name)
				_ <- validPhone(phone)
			} yield ()
		
	}
	
	override def login(implicit user: UserDto): OptionT[Future, Boolean] =
		for {
			enteredPw <- OptionT.fromOption[Future](user.userPw)
			userId <- OptionT.fromOption[Future](user.userId)
			correctPw <- userModel getUserPassword userId
		} yield enteredPw.equals(correctPw)
	
	private def commonRegister(f: UserDto => Option[ValidationResult[ValidationFailure, Unit]])
							  (implicit user: UserDto): Future[Either[ValidationFailure, Unit]] =
		(for {
			validationResult <- OptionT.fromOption[Future](f(user))
			affResult <- OptionT.liftF (validationResult onSuccess userModel.insertUser(user))
		} yield affResult) map {
			case Right(aff) if aff == 1 => Right()
			case Right(_) => Left(ValidationFailure("회원가입에 실패했습니다."))
			case Left(failure) => Left(failure)
		} getOrElse(throw new NoSuchElementException)
	
	override def register(implicit user: UserDto): Future[Either[ValidationFailure, Unit]] =
		commonRegister(_.accountValidation)
		
	override def kakaoRegister(implicit user: UserDto): Future[Either[ValidationFailure, Unit]] =
		commonRegister(_.kakaoAccountValidation)
		
	// TODO: Access Token이 유효하지 않을 경우
	override def getUserInfoByKakaoAccessToken(accessToken: String): Future[(String, String)] = {
		ws.url("https://kapi.kakao.com/v2/user/me")
				.addHttpHeaders("Authorization" -> s"Bearer $accessToken")
				.get()
				.map { response =>
					val jsonBody = response.json
					val email = (jsonBody \ "kakao_account" \ "email").as[String]
					val id = (jsonBody \ "id").as[Long].toString
					(email, id)
				}
	}
	
	override def findId(email: String): Future[Option[String]] =
		userModel.getUserByEmail(email).map(_.flatMap(_.userId))
	
	override def getUser(userId: String): Future[UserDto] =
		userModel getUserById userId getOrElse(throw new Exception)
	
	override def getUserOption(userId: String): OptionT[Future, UserDto] =
		userModel getUserById userId
	
	override def getUserOptionByEmail(email: String): Future[Option[UserDto]] =
		userModel getUserByEmail email
	
	override def userIdExist(userId: String): Future[Boolean] =
		userModel userIdDoesNotExist userId map(!_)
	
}
