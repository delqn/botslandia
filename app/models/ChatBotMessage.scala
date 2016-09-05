package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

case class ChatBotMessage(
                           id: Long,
                           message: String
                         ) extends KeyedEntity[Long]

object ChatBotMessage {

  /*
  // returns existing user by email address, or null if not exists
  def apply(email: String): ChatBotMessage = {
    inTransaction {
      // findByEmailQ(email).headOption.getOrElse(null)
    }
  }
  */

  def allQ: Query[ChatBotMessage] = from(Database.chatBotMessagesTable) {
    message => select(message)
  }

  def findAll(): List[ChatBotMessage] = {
    inTransaction {
      allQ.toList
    }
  }

  def create(message: ChatBotMessage) {
    inTransaction {
      Database.chatBotMessagesTable.insert(message)
    }
  }
}