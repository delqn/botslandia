package controllers

import java.io.FileInputStream
import java.util.UUID

import play.api.Play
import play.api.mvc.{Action, Controller}

import com.google.common.io.BaseEncoding

import exceptions.GoogleAuthTokenExpired
import models.File


class Files extends Controller {

  lazy val oauth2 = new utils.OAuth2(Play.current)

  def fileUpload = Action(parse.multipartFormData) { request =>
    request.body.file("file").map { picture =>
      val filename = picture.filename
      val contentType = picture.contentType.getOrElse("text/html")
      val fstream = new FileInputStream(picture.ref.file)
      val ftext = BaseEncoding.base64.encode(Stream.continually(fstream.read).takeWhile(_ != -1).map(_.toByte).toArray)
      try {
        val user = oauth2.getUser(request.session)
        // Persist
        File.create(new File(0, user.id, filename, contentType, ftext))
        // TODO(delyan): this should return file ID perhaps?
        Ok(filename)
      } catch {
        case expired: GoogleAuthTokenExpired =>
          val state = UUID.randomUUID().toString
          Ok(views.html.index(None, Some(oauth2.getLoginURL(state)), List()))
            .withSession("oauth-state" -> state)
      }
    }.getOrElse {
      Redirect(routes.Application.index)
        .flashing("error" -> "Missing file")
    }
  }

  def file = Action {
    // TODO(delyan): File.get...
    Ok("{}").as("application/json")
  }

}
