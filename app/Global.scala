import controllers.routes
import models._
import play.api.db.DB
import play.api._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/10/12
 * Time: 8:51 PM
 */
// Import the session management, including the implicit threadLocalSession

import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

// Import the query language

import org.scalaquery.ql._

// Import the standard SQL types

import org.scalaquery.ql.TypeMapper._

// Use H2Driver which implements ExtendedProfile and thus requires ExtendedTables

import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

object Global extends GlobalSettings
{
  override def onStart(app: Application)
  {
    import play.api.Play.current
    Database.forDataSource(DB.getDataSource()) withSession {

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


    }
    super.onStart(app)

  }
}
