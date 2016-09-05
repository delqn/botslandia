package utils

import play.api.Application
import play.api.http.{MimeTypes, HeaderNames}
import play.api.libs.ws.WS
import play.api.mvc.Results

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext


class OAuth2(application: Application) {
  lazy val googleAuthId = application.configuration.getString("google.client.id").get
  lazy val googleAuthSecret = application.configuration.getString("google.client.secret").get

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
}
