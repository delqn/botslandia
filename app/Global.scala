import play.api._
import play.api.db.DB
import org.squeryl._
import org.squeryl.adapters.PostgreSqlAdapter

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    System.out.println(DB.getConnection()(app))
    SessionFactory.concreteFactory = Some(() => {
      System.out.println("--- Creating a session ---")
      Session.create(DB.getConnection()(app), new PostgreSqlAdapter)
    })
  }
}
