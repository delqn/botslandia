package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.mvc.{Action, Controller}
import play.api.http.HeaderNames
import play.api.libs.ws.WS
import play.api.Play
import play.api.libs.json._

import models.{ChatBotMessage, KikBotMessage}

import utils.Bot


class KikBot extends Controller {
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
}
