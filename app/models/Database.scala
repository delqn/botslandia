package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

object Database extends Schema {

  val usersTable = table[User]("users")
  val filesTable = table[File]("files")
  val chatBotMessagesTable = table[ChatBotMessage]("chatbotmessages")
  val kikBotMessagesTable = table[KikBotMessage]("kikbotmessages")
  val tokensTable = table[Token]("tokens")

  on(usersTable) {
    u => declare { u.id is autoIncremented("users_id_seq") }
  }

  on(filesTable) {
    f => declare { f.id is autoIncremented("files_id_seq") }
  }

  on(chatBotMessagesTable) {
    t => declare { t.id is autoIncremented("chatbotmessages_id_seq") }
  }

  on(kikBotMessagesTable) {
    t => declare { t.id is autoIncremented("kikbotmessages_id_seq") }
  }

  on(tokensTable) {
    t => declare { t.id is autoIncremented("tokens_id_seq") }
  }

}

