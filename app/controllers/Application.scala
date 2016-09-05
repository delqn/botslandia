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

  def api = Action {
    Ok("{}").as("application/json")
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

  def msBot() = Action { request =>
      Ok("ok")
  }
}
