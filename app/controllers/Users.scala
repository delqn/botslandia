package controllers

import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.mvc.{Action, Controller}
import play.api.http.HeaderNames
import play.api.libs.ws.WS
import play.api.Play

import exceptions.GoogleAuthTokenExpired
import models.File


object Users extends Controller {

  lazy val oauth2 = new utils.OAuth2(Play.current)

  def index = Action {
    request =>
      // TODO(delyan): check to see if we still have access to the user's profile
      // Check how much longer we have the token for and perhaps ask for reauth
      try{
        val user = oauth2.getUser(request.session)
        val files = File.findAll(user.id)
        Ok(views.html.index(Some(user), None, files))
      } catch {
        case expired: GoogleAuthTokenExpired =>
          val state = UUID.randomUUID().toString
          Ok(views.html.index(None, Some(oauth2.getLoginURL(state)), List()))
            .withSession("oauth-state" -> state)
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

}
