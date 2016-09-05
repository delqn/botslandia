package controllers

import java.util.UUID

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Play
import play.api.mvc.{Action, Controller}


class Auth extends Controller {

  lazy val oauth2 = new utils.OAuth2(Play.current)

  def oauthSuccess = Action { request =>
    Redirect(routes.Application.index)//.withSession("oauth-token" -> request.session.get("oauth-token"))
  }

  def loginGoogle = Action {
    val state = UUID.randomUUID().toString
    Redirect(oauth2.getLoginURL(state)).withSession("oauth-state" -> state)
  }

  def logout = Action {
    Redirect(routes.Application.index).withSession("oauth-token" -> "logged-out")
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

}
