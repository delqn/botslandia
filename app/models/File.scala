package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

case class File(id: Long, userid: Long, filename: String, content_type: String, file: String) extends KeyedEntity[Long]

object File {
  def allQ: Query[File] = inTransaction { from(Database.filesTable) { file => select(file) } }
  def findAll(userId: Long): List[File] = inTransaction { allQ.where(f => f.userid === userId).toList }
  def create(file: File) = inTransaction { Database.filesTable.insert(file) }
  def getByName(userId: Long, fileName: String) = inTransaction { allQ.where( f=> f.filename === fileName).single }
}