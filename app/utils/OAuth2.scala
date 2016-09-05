package utils

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

import play.api.Application
import play.api.http.MimeTypes
import play.api.mvc.Results
import play.api.Play.current
import play.api.mvc.Session
import play.api.http.HeaderNames
import play.api.libs.ws.WS
import play.api.libs.json._

import exceptions.GoogleAuthTokenExpired
import models.User


class OAuth2(application: Application) {
  lazy val googleAuthId = application.configuration.getString("google.client.id").get
  lazy val googleAuthSecret = application.configuration.getString("google.client.secret").get
  lazy val callbackUrl = application.configuration.getString("google.callback.url").getOrElse("")

  def exchangeAuthorizationCodeForTokens(code: String): Future[String] = {
    lazy val callbackUrl = application.configuration.getString("google.callback.url")
    val tokenResponse = WS.url("https://www.googleapis.com/oauth2/v3/token").withQueryString(
      "code" -> code,
      "client_id" -> googleAuthId,
      "client_secret" -> googleAuthSecret,
      "redirect_uri" -> callbackUrl.getOrElse(""),
      "grant_type" -> "authorization_code"
    ).withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON).post(Results.EmptyContent())


    // TODO(delyan): Do this ONLY if there's a failure - show the body of the message
    val res = Await.ready(tokenResponse, 10 seconds).value.get
    println(">>>>>\n", res map { r => r.body }, "\n<<<<<<<")

    tokenResponse.flatMap { response =>
      (response.json \ "access_token").asOpt[String]
        .fold(Future.failed[String](new IllegalStateException("Sod off!"))) {
          accessToken => Future.successful(accessToken)
        }
    }
  }

  def getLoginURL(state: String): String = {

    ("https://accounts.google.com/o/oauth2/auth" +
      "?redirect_uri=%s" +
      "&response_type=code" +
      "&client_id=%s" +
      "&state=%s" +
      "&scope=profile" +
      "&approval_prompt=force" +
      "&access_type=offline").format(callbackUrl, googleAuthId, state)
  }

  @throws(classOf[GoogleAuthTokenExpired])
  def getGoogleUserInfo(authToken: String): JsValue = {
    val res = Await.ready(
      WS.url(s"https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=$authToken").
        withHeaders(HeaderNames.AUTHORIZATION -> s"token $authToken").get(),
      2 seconds).value.get
    res match {
      case Success(wSResponse) => wSResponse.json
      case Failure(ex) => throw GoogleAuthTokenExpired("google auth token expired")
    }
  }

  @throws(classOf[GoogleAuthTokenExpired])
  def getUser(session: Session): User = {
    session.get("oauth-token").map { token =>
      val userInfo = getGoogleUserInfo(token)
      userInfo \ "error" match {
        case _: JsUndefined => true // whatever
        case _ => throw GoogleAuthTokenExpired("some error")
      }
      val email = "unknown"
      val name = (userInfo \ "name").asOpt[String]
      val locale = (userInfo \ "locale").asOpt[String]
      val gender = (userInfo \ "gender").asOpt[String]
      val picture = (userInfo \ "picture").as[String] // TODO(delyan): should be asOpt
    val google_user_id = (userInfo \ "id").as[String]
      val user = new models.User(0, google_user_id, email, "password", name.toString, false, gender.toString, locale.toString, picture.toString) // TODO: get the user from DB
      // TODO(delyan): optimize the following 2 lines
      User.create(user)
    }.getOrElse {
      throw GoogleAuthTokenExpired("some other error")
    }
  }

}
