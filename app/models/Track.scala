package models

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/8/12
 * Time: 6:54 PM
 */

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

import java.sql.Date

case class Track(id: Long, albumID: Long, name: String, download: Boolean = true, price: Double = 7.00,
                 artistName: Option[String],
                 art: Option[String], lyrics: Option[String], about: Option[String], credits: Option[String], isrc: Option[String], releaseDate: Option[Date])

object Track extends Table[Track]("tracks") with DataTable
{


  def albumID = column[Long]("album_id", O NotNull)


  def download = column[Boolean]("download")


  def lyrics = column[Option[String]]("about", O.Nullable, O DBType ("text"))


  def isrc = column[Option[String]]("about", O.Nullable, O DBType ("varchar(10)"))

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name", O.NotNull)

  def about = column[Option[String]]("about", O.Nullable, O DBType ("text"))

  def price = column[Double]("price")

  def credits = column[Option[String]]("about", O.Nullable, O DBType ("text"))

  def artistName = column[Option[String]]("artistName", O.Nullable, O DBType ("varchar(45)"))

  def art = column[Option[String]]("art", O.Nullable, O DBType ("varchar(45)"))

  def releaseDate = column[Option[Date]]("releaseDate", O Nullable)


  def * = id ~ albumID ~ name ~ download ~ price ~ artistName ~ art ~ lyrics ~ about ~ credits ~ isrc ~ releaseDate <>(Track.apply _, Track.unapply _)

  def album = foreignKey("album_id", albumID, Albums)(_.id)

}
