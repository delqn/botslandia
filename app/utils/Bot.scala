package utils

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.{Failure, Success}

import play.api.Play.current
import play.api.libs.ws.WS


object Bot {

  def duck(qry: String): String = {
    val res = Await.ready(
      WS.url(s"http://api.duckduckgo.com/?format=json&q=$qry").get,
      5 seconds).value.get
    res match {
      case Success(wsResponse) => (wsResponse.json \\ "Text").head.toString.replace("\"", "")
      case Failure(ex) => ex.toString
    }
  }

  def parseInputGetOutput(input: String): String = {
    val words = "\\w+".r.findAllIn(input).toList
    words.head match {
      case "duck" => duck(words.tail.mkString("%20"))
      case x => duck(words.mkString("%20"))
    }
  }

}
