package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

case class ChatBotMessage(id: Long, message: String) extends KeyedEntity[Long]

object ChatBotMessage {
  def allQ: Query[ChatBotMessage] = from(Database.chatBotMessagesTable) { msg => select(msg) }
  def findAll(): List[ChatBotMessage] = inTransaction { allQ.toList }
  def create(message: ChatBotMessage) = inTransaction { Database.chatBotMessagesTable.insert(message) }

}