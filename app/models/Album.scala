package models

import play.api.db._
import anorm._
import play.api.Play.current


import anorm.SqlParser._


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/4/12
 * Time: 1:11 PM
 */

import java.sql.{Date, Time, Timestamp}
import play.api.db._
import play.api.Play.current

// Import the session management, including the implicit threadLocalSession

import org.scalaquery.session._


// Import the query language

import org.scalaquery.ql._

// Import the standard SQL types

import org.scalaquery.ql.TypeMapper._

// Use H2Driver which implements ExtendedProfile and thus requires ExtendedTables


import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}


case class Album(id: Long, artistID: Long, name: String, artistName: Option[String], slug: String = "", public: Boolean = true, active: Boolean = false,
                 download: Boolean = true, donateMore: Boolean = true, price: Double = 7.00,
                 art: Option[String], about: Option[String], credits: Option[String], upc: Option[String], releaseDate: Option[Date])
{

}


object Albums extends Table[Album]("albums") with DataTable
{

  def id = column[Long]("id", O.PrimaryKey, O AutoInc)

  def artistID = column[Long]("artis_id")

  def name = column[String]("name", O.NotNull)

  def slug = column[String]("slug")

  def artistName = column[Option[String]]("artistName", O.Nullable, O DBType ("varchar(45)"))

  def public = column[Boolean]("public", O Default (true))

  def active = column[Boolean]("active", O Default (false))

  def download = column[Boolean]("download", O Default (true))


  def donateMore = column[Boolean]("donateMore", O Default (true))

  def price = column[Double]("price")

  def art = column[Option[String]]("art", O.Nullable, O DBType ("varchar(45)"))

  def about = column[Option[String]]("about", O.Nullable, O DBType ("text"))

  def credits = column[Option[String]]("credits", O.Nullable, O DBType ("text"))

  def upc = column[Option[String]]("upc", O.Nullable, O DBType ("varchar(20)"))


  def releaseDate = column[Option[Date]]("releaseDate", O Nullable)


  def * = id ~ artistID ~ name ~ artistName ~ slug ~ public ~ active ~ download ~ donateMore ~ price ~ art ~ about ~ credits ~ upc ~ releaseDate <>(Album.apply _, Album.unapply _)


  def bySlug(artistId: Long, slug: String): Option[Album] =
  {
    db withSession {
      implicit s =>
        (for {
          a <- Albums if a.artistID === artistId && a.name === slug.bind
        } yield a).firstOption
    }
  }

  def validateSlug(slug: String): Boolean =
  {
    db withSession {
      implicit s =>

        val count = (for {
          a <- Albums if a.slug === slug.bind
        } yield a.slug.count).first
        count != 0
    }
  }


}



