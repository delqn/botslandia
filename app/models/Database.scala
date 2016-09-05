package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

object Database extends Schema {
  val usersTable = table[User]("users")
  val filesTable = table[File]("files")
  val chatBotMessagesTable = table[ChatBotMessage]("chatbotmessages")
  val kikBotMessagesTable = table[KikBotMessage]("kikbotmessages")

  on(usersTable) {
    u => declare { u.id is autoIncremented("users_id_seq") }
  }

  on(filesTable) {
    f => declare { f.id is autoIncremented("files_id_seq") }
  }

  on(chatBotMessagesTable) {
    f => declare { f.id is autoIncremented("files_id_seq") }
  }

  on(kikBotMessagesTable) {
    f => declare { f.id is autoIncremented("files_id_seq") }
  }

}

