package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

case class User(id: Long,
                google_user_id: String,
                email: String,
                password: String,
                fullname: String,
                isadmin: Boolean,
                gender: String,
                locale: String,
                picture: String
               ) extends KeyedEntity[Long]

object User {

  // returns existing user by email address, or null if not exists
  def apply(email: String): User = inTransaction { findByEmailQ(email).headOption.orNull }
  def allQ: Query[User] = from(Database.usersTable) { user => select(user) }
  def findByGoogleID(google_user_id: String): Query[User] = from(allQ) {
    user => where(user.google_user_id === google_user_id).select(user)
  }
  def findByEmailQ(email: String): Query[User] = from(allQ) { user => where(user.email === email).select(user) }
  def findAll(): List[User] = inTransaction { allQ.toList }
  def create(user: User): User = {
    inTransaction {
    val users = User.findByGoogleID(user.google_user_id)
      if(users.size < 1) {
        Database.usersTable.insert(user)
        User.findByGoogleID(user.google_user_id).single
      } else {
        users.single
      }
    }
  }

}