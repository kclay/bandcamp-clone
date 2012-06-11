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
      (Accounts.ddl ++ Genres.ddl).create

      (for {g <- Genres} yield g).take(1).firstOption.getOrElse {
        Genres.insertAll(
          Genre(1, "Acoustic"),
          Genre(2, "Alternative"),
          Genre(3, "Ambient"),
          Genre(4, "Blues"),
          Genre(5, "Classical"),
          Genre(6, "Comedy"),
          Genre(7, "Country"),
          Genre(8, "Devotional"),
          Genre(9, "Electronic"),
          Genre(10, "Experimental"),
          Genre(11, "Folk"),
          Genre(12, "Funk"),
          Genre(13, "Hip Hop/Rap"),
          Genre(14, "Jazz"),
          Genre(15, "Kids"),
          Genre(16, "Latin"),
          Genre(17, "Metal"),
          Genre(18, "Pop"),
          Genre(19, "Punk"),
          Genre(20, "R&B/Soul"),
          Genre(21, "Reggae"),
          Genre(22, "Rock"),
          Genre(23, "Soundtrack"),
          Genre(24, "Spoken Word"),
          Genre(25, "World")


        )
      }

    }
    super.onStart(app)

  }
}
