package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

case class KikBotMessage(
                           id: Long,
                           message: String
                         ) extends KeyedEntity[Long]

object KikBotMessage {

  /*
  // returns existing user by email address, or null if not exists
  def apply(email: String): kikBotMessage = {
    inTransaction {
      // findByEmailQ(email).headOption.getOrElse(null)
    }
  }
  */

  def allQ: Query[KikBotMessage] = from(Database.kikBotMessagesTable) {
    message => select(message)
  }

  def findAll(): List[KikBotMessage] = {
    inTransaction {
      allQ.toList
    }
  }

  def create(message: KikBotMessage) {
    inTransaction {
      Database.kikBotMessagesTable.insert(message)
    }
  }
}