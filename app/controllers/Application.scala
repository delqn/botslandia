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

case class GoogleAuthTokenExpired(msg: String) extends Exception(msg)

object Application extends Controller {

  lazy val oauth2 = new utils.OAuth2(Play.current)

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

  def api = Action {
    Ok("{}").as("application/json")
  }



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

  // -----------------

  def oauthSuccess = Action { request =>
      Redirect(routes.Application.index)//.withSession("oauth-token" -> request.session.get("oauth-token"))
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

  def chatBot() = Action.async { request =>
    implicit val app = Play.current
    ChatBotMessage.create(new ChatBotMessage(0, request.toString() + "  #  " + request.body.toString ))
    // TODO(delyan): there's got to be a better way of composing safe JSON
    // TODO(delyan) -- the ID is scoped and is NOT the fbid of the user
    // AnyContentAsJson({"object":"page","entry":[{"id":577706005731513,"time":1460574584412,"messaging":[{"sender":{"id":964035286979585},"recipient":{"id":577706005731513},"timestamp":1460574584387,"message":{"mid":"mid.1460574584374:3b6228a24ce3aa5244","seq":7,"text":"sntaheou"}}]}]})
    // This stuff here is going to break?!
    var body: String = ""
    var text: String = ""
    try {
      val messaging = ((((request.body.asJson.get \ "entry")(0)) \ "messaging")(0))
      val fbid = ((messaging \ "sender") \ "id").toString
      val query = ((messaging \ "message") \ "text").toString.replace("\"", "")
      text = Bot.parseInputGetOutput(query)
      // body = Json.parse(s"""{"recipient": { "id": "$fbid" }, "message": {"text": $text}} """).toString
      body = Json.parse(
        s"""
           |{
           |  "recipient":{ "id":"$fbid" },
           |  "message":{
           |    "attachment":{
           |      "type":"template",
           |      "payload":{
           |        "template_type":"button",
           |        "text":"$text",
           |        "buttons":[
           |          {
           |            "type":"web_url",
           |            "url":"https://duckduckgo.com/?q=$query",
           |            "title":"Show Website"
           |          },
           |          {
           |            "type":"postback",
           |            "title":"Start Chatting",
           |            "payload":"USER_DEFINED_PAYLOAD"
           |          }
           |        ]
           |      }
           |    }
           |  }
           |}
         """.stripMargin).toString
    } catch {
      case e: Throwable => println(e)
    }

    // TODO(delyan): move this to config
    println("`````````````````` >> ", body)
    val token = sys.env("FB_TOKEN")
    WS.url(s"https://graph.facebook.com/v2.6/me/messages?access_token=$token").
      withHeaders(HeaderNames.AUTHORIZATION -> s"token $token", HeaderNames.CONTENT_TYPE -> "application/json").
      post(body).map { response =>
      ChatBotMessage.create(new ChatBotMessage(0, response.toString + "  #  " + response.body.toString ))
      Ok(request.getQueryString("hub.challenge").getOrElse(text)) }
  }

  def kikBot() = Action.async { request =>
    implicit val app = Play.current
    val token = sys.env("KIK_TOKEN")
    var body: JsValue = null
    var text: String = null
    try {
      val message = (request.body.asJson.get \ "messages")(0)
      text = Bot.parseInputGetOutput((message \ "body").toString)
      val to = (message \ "from").toString
      val chatId = (message \ "chatId").toString
      body = Json.parse(s"""{
        "messages": [
            {
                "body": "$text",
                "to": $to,
                "type": "text",
                "chatId": $chatId
            }
        ]}""")
    } catch {
      case e: Throwable => println(e)
    }
    KikBotMessage.create(new KikBotMessage(0, request.toString() + "  #  " + request.body.toString ))
    WS.url("https://api.kik.com/v1/message").
      withHeaders(HeaderNames.AUTHORIZATION -> s"Basic $token").
      post(body).map { response =>
      ChatBotMessage.create(new ChatBotMessage(0, response.toString + "  #  " + response.body.toString ))
      Ok(text) }
  }

  def kikBotGetConfig() = Action.async { request =>
    implicit val app = Play.current
    val token = sys.env("KIK_TOKEN")
    WS.url("https://api.kik.com/v1/config").
      withHeaders(HeaderNames.AUTHORIZATION -> s"Basic $token", HeaderNames.CONTENT_TYPE -> "application/json").
      get().map { response => Ok(response.json.toString) }
  }

  def msBot() = Action { request =>
      Ok("ok")
  }
}
