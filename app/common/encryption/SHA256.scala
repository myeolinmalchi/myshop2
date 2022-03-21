package common.encryption

import java.security.MessageDigest

object SHA256 {
	def encrypt(str: String): String =
		MessageDigest.getInstance("SHA-256")
				.digest(str.getBytes("UTF-8"))
				.map("%02x".format(_)).mkString
}
