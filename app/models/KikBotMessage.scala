package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

case class KikBotMessage(id: Long, message: String) extends KeyedEntity[Long]

object KikBotMessage {
  def allQ: Query[KikBotMessage] = from(Database.kikBotMessagesTable) { msg => select(msg) }
  def findAll(): List[KikBotMessage] = inTransaction { allQ.toList }
  def create(message: KikBotMessage) = inTransaction { Database.kikBotMessagesTable.insert(message) }
}