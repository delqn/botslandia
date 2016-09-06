package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

case class Token(id: Long, userid: Long, service: String, token: String) extends KeyedEntity[Long]

object Token {

  def apply(userid: Long, service: String): Token = inTransaction { find(userid, service).headOption.orNull }
  def allQ: Query[Token] = from(Database.tokensTable) { token => select(token) }
  def find(userid: Long, service: String): Query[Token] = from(allQ) {
    token => where(token.userid === userid and token.service === service).select(token)
  }
  def findAll(userid: Long): List[Token] = inTransaction { allQ.where(_.userid === userid).toList }
  def create(token: Token): Token = inTransaction { Database.tokensTable.insert(token) }
  def delete(id: Long) = inTransaction { Database.tokensTable.deleteWhere { token => token.id === id} }

}
