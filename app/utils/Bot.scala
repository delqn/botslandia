package utils

import play.api.Play

import scala.concurrent.duration._
import scala.concurrent.Await
import play.api.libs.ws.WS
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global // TODO(delyan ) -- what is this

/**
  * Created by de on 4/15/16.
  */
object Bot {
  def duck(qry: String): String = {
    // TODO(delyan ) -- what is this
    implicit val app = Play.current
    val res = Await.ready(
      WS.url(s"http://api.duckduckgo.com/?format=json&q=$qry").get,
      5 seconds).value.get
    res match {
      case Success(wsResponse) => { println(wsResponse); (wsResponse.json \\ "Text").head.toString.replace("\"", "") }
      case Failure(ex) => ex.toString
    }
  }

  val word = "\\w+".r
  def parseInputGetOutput(input: String): String = {
    val words = word.findAllIn(input).toList
    words.head match {
      case "duck" => duck(words.tail.mkString("%20"))
      case x => duck(words.mkString("%20"))
    }
  }
}
