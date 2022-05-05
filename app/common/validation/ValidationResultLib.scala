package common.validation

import cats.Monad
import cats.data.{EitherT, OptionT}

trait ValidationResultLib[M[_]] {
	
	case class ValidationFailure(msg: String)
	
	type ValidationResult[F, S] = EitherT[M, F, S]
	
	object ValidationResult {
		
		def successful[F, S](s: S)(implicit m: Monad[M]): ValidationResult[F, S] =
			EitherT.rightT(s)
		
		def failed[F, S](f: F)(implicit m: Monad[M]): ValidationResult[F, S] =
			EitherT.leftT(f)
		
		def ensure[F](c: => Boolean, onFailure: => F)(implicit m: Monad[M]): ValidationResult[F, Unit] =
			EitherT.cond[M](c, (), onFailure)
		
		def ensureM[F](c: => M[Boolean], onFailure: => F)(implicit m: Monad[M]): ValidationResult[F, Unit] =
			EitherT.right(c).ensure(onFailure)(b => b).map(_ => ())
		
		def fromOptionM[F, S](opt: M[Option[S]], ifNone: => F)(implicit m: Monad[M]): ValidationResult[F, S] =
			EitherT.fromOptionF(opt, ifNone)
			
		def apply[F, S](v: M[Either[F, S]])(implicit m: Monad[M]): ValidationResult[F, S] =
			EitherT.apply(v)
		
	}
	
	implicit class ValidationResultOps[F, S](vr: ValidationResult[F, S]) {
		
		def onSuccess[S2](s2: => M[S2])(implicit m: Monad[M]): M[Either[F, S2]] =
			vr.onSuccess(_ => s2)
		
		def onSuccess[S2](fn: S => M[S2])(implicit m: Monad[M]): M[Either[F, S2]] =
			vr.semiflatMap(fn).value
	}
}
