package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

case class File(id: Long, userid: Long, filename: String, content_type: String, file: String) extends KeyedEntity[Long]

object File {
  def allQ: Query[File] = from(Database.filesTable) { file => select(file) }
  def findAll(): List[File] = inTransaction { allQ.toList }
  def create(file: File) = inTransaction { Database.filesTable.insert(file) }
}