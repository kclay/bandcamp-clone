import java.util
import org.squeryl.adapters.{H2Adapter, MySQLAdapter}
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.{Session, SessionFactory}
import play.api.db.DB
import play.api.GlobalSettings

import play.api.Application
import play.api.mvc._
import play.mvc.Http
import utils.Context

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/10/12
 * Time: 8:51 PM
 */
// Import the session management, including the implicit threadLocalSession

object Global extends GlobalSettings {

  import Results._

  /*case class RestoreSessionRequest(token: String, request: RequestHeader)
  extends WrappedRequest(request)
{

  override lazy val cookies: Cookies = Cookies(Option(token))

}  */

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
     Context.current()
    val results=super.onRouteRequest(request)
    Context.remove()
    results
  }

  val dbAdapter = new H2Adapter();

  override def onStart(app: Application) {
    SessionFactory.concreteFactory = app.configuration.getString("db.default.driver") match {
      case Some("org.h2.Driver") => Some(() => getSession(new H2Adapter, app))
      case Some("com.mysql.jdbc.Driver") => Some(() => getSession(new MySQLAdapter, app))
      case _ => sys.error("Database driver must be either org.h2.Driver or org.postgresql.Driver")
    }
    import org.squeryl.PrimitiveTypeMode._
    import models.SiteDB;
    inTransaction(SiteDB.printDdl)

    super.onStart(app)
  }

  def getSession(adapter: DatabaseAdapter, app: Application) = Session.create(DB.getConnection()(app), adapter)

  /*SessionFactory.concreteFactory = Some(
() => Session.create(DB.getDataSource().getConnection(),
dbAdapter))    */
  /* Database.forDataSource(SiteDB$.getDataSource()) withSession {

val create = {

(for {a <- Artists} yield a).firstOption.getOrElse {
(Albums.ddl ++ Artists.ddl ++ Genres.ddl ++ Tracks.ddl ++ ArtistTags.ddl ++ Tags.ddl).create

Artists.create("cideas", "cideas", "a@yahoo.com", "hello boy")

Genres.insertAll(
  Genre(1, "Acoustic"),
  Genre(2, "Alternative"),
  Genre(3, "Ambient"),
  Genre(4, "Blues"),
  Genre(5, "Classical"),
  Genre(6, "Comedy"),
  Genre(7, "Country"),
  Genre(9, "Devotional"),
  Genre(10, "Electronic"),
  Genre(11, "Experimental"),
  Genre(12, "Folk"),
  Genre(13, "Funk"),
  Genre(14, "Hip Hop/Rap"),
  Genre(15, "Jazz"),
  Genre(16, "Kids"),
  Genre(17, "Latin"),
  Genre(18, "Metal"),
  Genre(19, "Pop"),
  Genre(20, "Punk"),
  Genre(21, "R&B/Soul"),
  Genre(22, "Reggae"),
  Genre(23, "Rock"),
  Genre(24, "Soundtrack"),
  Genre(25, "Spoken Word"),
  Genre(26, "World")


)
Tags.insertAll(
  Tag(1, "hip hop"),
  Tag(2, "jazz"),
  Tag(3, "pop"),
  Tag(4, "country")
)
}
}
import util.control.Exception.allCatch
allCatch either {
(for {a <- Artists} yield a).take(1).firstOption.getOrElse {
true
} match {
case _ => create
}
}


}      */


}
