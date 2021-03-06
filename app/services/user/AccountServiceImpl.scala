package services.user

import cats.data.OptionT
import common.validation.ValidationResultLib
import common.validation.ValidationResultLib.ValidationFailureTemp
import dto.UserRequestDto.{EMAIL, NAME, NONE_MATCH_MSG, OVERLAP_MSG, PATTERNS, PHONE, USER_ID, USER_PW}
import dto.{UserDto, UserRequestDto}
import javax.inject.{Inject, Singleton}
import models.UserModel
import play.api.libs.ws.WSClient
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class AccountServiceImpl @Inject()()(implicit ws: WSClient, ec: ExecutionContext, userModel: UserModel)
		extends AccountService with ValidationResultLib[Future] {
	
	implicit class UserValidator(user: UserRequestDto) {
		private def checkPattern(str: String, key: String): ValidationResult[ValidationFailureTemp, Unit] =
			ValidationResult.ensure(
				str.matches(PATTERNS(key)),
				onFailure = ValidationFailureTemp(NONE_MATCH_MSG(key))
			)
		
		private def validUserId(userId: String): ValidationResult[ValidationFailureTemp, Unit] =
			for {
				_ <- checkPattern(userId, USER_ID)
				_ <- ValidationResult.ensureM(
					userModel userIdDoesNotExist userId,
					onFailure = ValidationFailureTemp(OVERLAP_MSG(USER_ID))
				)
			} yield ()
		
		private def validUserPw(userPw: String): ValidationResult[ValidationFailureTemp, Unit] =
			checkPattern(userPw, USER_PW)
		
		private def validName(name: String): ValidationResult[ValidationFailureTemp, Unit] =
			checkPattern(name, NAME)
		
		private def validEmail(email: String): ValidationResult[ValidationFailureTemp, Unit] =
			for {
				_ <- checkPattern(email, EMAIL)
				_ <- ValidationResult.ensureM(
					userModel emailDoesNotExist email,
					onFailure = ValidationFailureTemp(OVERLAP_MSG(EMAIL))
				)
			} yield ()
		
		private def validPhone(phone: String): ValidationResult[ValidationFailureTemp, Unit] =
			checkPattern(phone, PHONE)
		
		def accountValidation: Option[ValidationResult[ValidationFailureTemp, UserDto]] =
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
			} yield UserDto(userId, userPw, name, email, phone)
		
		def kakaoAccountValidation: Option[ValidationResult[ValidationFailureTemp, UserDto]] =
			for {
				userId <- user.userId
				userPw <- user.userPw
				name <- user.name
				email <- user.email
				phone <- user.phonenumber
			} yield for {
				_ <- validName(name)
				_ <- validPhone(phone)
			} yield UserDto(userId, userPw, name, email, phone)
	}
	
	override def login(implicit user: UserRequestDto): OptionT[Future, Boolean] =
		for {
			enteredPw <- OptionT.fromOption[Future](user.userPw)
			userId <- OptionT.fromOption[Future](user.userId)
			correctPw <- userModel getUserPassword userId
		} yield enteredPw.equals(correctPw)
	
	private def commonRegist(f: UserRequestDto => Option[ValidationResult[ValidationFailureTemp, UserDto]])
							(implicit user: UserRequestDto): Future[Either[ValidationFailureTemp, Unit]] =
		(for {
			validationResult <- OptionT.fromOption[Future](f(user))
			affResult <- OptionT.liftF(
				validationResult.onSuccess { user => userModel insertUser user }
			)
		} yield affResult) map {
			case Right(aff) if aff == 1 => Right()
			case Right(_) => Left(ValidationFailureTemp("??????????????? ??????????????????."))
			case Left(failure) => Left(failure)
		} getOrElse (throw new NoSuchElementException("????????? ????????? ????????????."))
	
	override def regist(implicit user: UserRequestDto): Future[Either[ValidationFailureTemp, Unit]] =
		commonRegist(_.accountValidation)
	
	override def kakaoRegist(implicit user: UserRequestDto): Future[Either[ValidationFailureTemp, Unit]] =
		commonRegist(_.kakaoAccountValidation)
	
	// TODO: Access Token??? ???????????? ?????? ??????
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
	
	override def getUser(userId: String): OptionT[Future, UserDto] =
		userModel getUserById userId
	
	override def getUserByEmail(email: String): OptionT[Future, UserDto] =
		userModel getUserByEmail email
	
	override def userIdExist(userId: String): Future[Boolean] =
		userModel userIdDoesNotExist userId map (!_)
	
	override def updateUserPw(userPw: String): Future[Int] =
		userModel updateUserPw userPw
		
	override def updateName(name: String): Future[Int] =
		userModel updateName name
		
	
}
