package controllers

import com.google.common.io.BaseEncoding
import play.api.mvc.{Action, Controller, Session}
import java.io.FileInputStream

import models.{ChatBotMessage, File, KikBotMessage, User}
import java.util.UUID

import play.api.http.HeaderNames
import play.api.libs.ws.WS
import play.api.Play
import play.api.libs.json._
import utils.Bot

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}


class Auth extends Controller {


  lazy val oauth2 = new utils.OAuth2(Play.current)


  def oauthSuccess = Action { request =>
    Redirect(routes.Application.index)//.withSession("oauth-token" -> request.session.get("oauth-token"))
  }

  def getLoginURL(state: String): String = {
    lazy val googleAuthId = Play.current.configuration.getString("google.client.id").get
    lazy val callbackUrl = Play.current.configuration.getString("google.callback.url").getOrElse("")
    ("https://accounts.google.com/o/oauth2/auth" +
      "?redirect_uri=%s" +
      "&response_type=code" +
      "&client_id=%s" +
      "&state=%s" +
      "&scope=profile" +
      "&approval_prompt=force" +
      "&access_type=offline").format(callbackUrl, googleAuthId, state)
  }

  def index = Action {
    request =>
      // TODO(delyan): check to see if we still have access to the user's profile
      // Check how much longer we have the token for and perhaps ask for reauth
      try{
        val user = getUser(request.session)
        var files = File.findAll()
        Ok(views.html.index(Some(user), None, files))
      } catch {
        case expired: GoogleAuthTokenExpired => {
          val state = UUID.randomUUID().toString
          Ok(views.html.index(None, Some(getLoginURL(state)), List())).withSession("oauth-state" -> state)
        }
      }
  }

  def loginGoogle = Action {
    val state = UUID.randomUUID().toString
    Redirect(getLoginURL(state)).withSession("oauth-state" -> state)
  }

  def logout = Action {
    Redirect(routes.Application.index).withSession("oauth-token" -> "logged-out")
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
      val user = new User(0, google_user_id, email, "password", name.toString, false, gender.toString, locale.toString, picture.toString) // TODO: get the user from DB
      // TODO(delyan): optimize the following 2 lines
      User.create(user)
    }.getOrElse {
      throw GoogleAuthTokenExpired("some other error")
    }
  }

  @throws(classOf[GoogleAuthTokenExpired])
  def getGoogleUserInfo(authToken: String): JsValue = {
    implicit val app = Play.current
    val res = Await.ready(
      WS.url(s"https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=$authToken").
        withHeaders(HeaderNames.AUTHORIZATION -> s"token $authToken").get(),
      2 seconds).value.get
    res match {
      case Success(wSResponse) => wSResponse.json
      case Failure(ex) => throw GoogleAuthTokenExpired("google auth token expired")
    }
  }

  def userProfile() = Action.async { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").fold(Future.successful(Unauthorized("No way Jose"))) { authToken =>
      WS.url(s"https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=$authToken").
        withHeaders(HeaderNames.AUTHORIZATION -> s"token $authToken").
        get().map { response => {
        Ok(views.html.userInfo(
          (response.json \ "id").as[String],
          (response.json \ "name").as[String],
          (response.json \ "given_name").as[String],
          (response.json \ "family_name").as[String],
          (response.json \ "link").as[String],
          (response.json \ "picture").as[String],
          (response.json \ "gender").as[String],
          (response.json \ "locale").as[String]))
      }
      }
    }
  }

  def userInfo() = Action.async { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").fold(Future.successful(Unauthorized("No way Jose"))) { authToken =>
      WS.url(s"https://www.googleapis.com/plus/v1/people/me?alt=json&access_token=$authToken").
        withHeaders(HeaderNames.AUTHORIZATION -> s"token $authToken").
        get().map { response => Ok(response.json) }
    }
  }

  def oauthCallback(codeOpt: Option[String] = None, stateOpt: Option[String] = None) = Action.async { implicit request =>
    (for {
      code <- codeOpt
      state <- stateOpt
      oauthState <- request.session.get("oauth-state")
    } yield {
      if (state == oauthState) {
        oauth2.exchangeAuthorizationCodeForTokens(code).map { accessToken =>
          Redirect(routes.Application.oauthSuccess).withSession("oauth-token" -> accessToken)
        }.recover {
          case ex: IllegalStateException => Unauthorized(ex.getMessage)
        }
      }
      else {
        Future.successful(BadRequest("Invalid google login"))
      }
    }).getOrElse(Future.successful(BadRequest("No parameters supplied")))
  }


  def userInfoJson() = Action.async { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").fold(Future.successful(Unauthorized("No way Jose"))) { authToken =>
      WS.url(s"https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=$authToken").
        withHeaders(HeaderNames.AUTHORIZATION -> s"token $authToken").
        get().map { response => Ok(response.json) }
    }
  }

  def tokenInfo() = Action.async { request =>
    // val url = "https://www.googleapis.com/plus/v1/people/me"
    implicit val app = Play.current
    request.session.get("oauth-token").fold(Future.successful(Unauthorized("No way Jose"))) { authToken =>
      WS.url(s"https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=$authToken").
        withHeaders(HeaderNames.AUTHORIZATION -> s"token $authToken").
        get().map { response => Ok(response.json) }
    }
  }


}
