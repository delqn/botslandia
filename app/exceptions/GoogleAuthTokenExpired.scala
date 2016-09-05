package exceptions

case class GoogleAuthTokenExpired(msg: String) extends Exception(msg)