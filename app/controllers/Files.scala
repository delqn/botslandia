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

class Files extends Controller {
  def fileUpload = Action(parse.multipartFormData) { request =>
    request.body.file("file").map { picture =>
      val filename = picture.filename
      val contentType = picture.contentType.getOrElse("text/html")
      val fstream = new FileInputStream(picture.ref.file)
      val ftext = BaseEncoding.base64.encode(Stream.continually(fstream.read).takeWhile(_ != -1).map(_.toByte).toArray)
      try{
        val user = getUser(request.session)
        File.create(new File(0, user.id, filename, contentType, ftext)) // push to the database
        Ok(filename) // TODO(delyan): this should return file id or something
      } catch {
        case expired: GoogleAuthTokenExpired => {
          val state = UUID.randomUUID().toString
          Ok(views.html.index(None, Some(getLoginURL(state)), List())).withSession("oauth-state" -> state)
        }
      }
    }.getOrElse {
      Redirect(routes.Application.index).flashing("error" -> "Missing file")
    }
  }

  def file = Action {
    //File.get
    Ok("{}").as("application/json")
  }
}
