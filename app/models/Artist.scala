package models

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/4/12
 * Time: 12:10 PM
 */

import play.api.db._
import anorm._
import models._
import anorm.SqlParser._
import play.api.Play.current
import org.apache.commons.codec.digest.DigestUtils._
import java.sql.Clob
import java.security.SecureRandom

import org.scalaquery.session._
import javax.crypto.{SecretKey, SecretKeyFactory}
import java.security.spec.KeySpec;
import java.security.Key;
import javax.crypto.spec.{SecretKeySpec, PBEKeySpec}
import utils.PasswordEncoder
import play.api.Logger


// Import the query language

import org.scalaquery.ql._

// Import the standard SQL types

import org.scalaquery.ql.TypeMapper._

// Use H2Driver which implements ExtendedProfile and thus requires ExtendedTables

import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}


case class Artist(id: Long, username: String, password: String, email: String, name: String, domain: String, permission: Permission = NormalUser, activated: Boolean = false)


/*object Artist$ extends Table[(Long,String,String,String,Option[Permission],String]{

}*/


case class Genre(id: Int, name: String)

object Genres extends Table[Genre]("generes") with DataTable
{

  def id = column[Int]("id", O PrimaryKey, O AutoInc)

  def name = column[String]("name")

  def * = id ~ name <>(Genre.apply _, Genre.unapply _)

  def allAsString: Seq[(String, String)] =
  {
    all.map {
      g => (g.id.toString, g.name)
    }
  }

  def all: List[Genre] =
  {
    db withSession {
      implicit s =>
        (for {g <- Genres} yield g).list
    }
  }
}

case class ArtistTag(artistID: Long, tagID: Int)

object ArtistTags extends Table[ArtistTag]("artist_tags") with DataTable
{

  def artistID = column[Long]("artist_id")

  def tagID = column[Int]("tag_id")

  def * = artistID ~ tagID <>(ArtistTag.apply _, ArtistTag.unapply _)

  def flat = artistID ~ tagID

}

object Artists extends Table[Artist]("artists") with DataTable
{

  private val SALT: String = "m^c*$kxz_qkwupq$by*fpi_czho=#8+k5dnakvd7x$gt#-&h+t";

  implicit object PermissionTypeMapper extends MappedTypeMapper[Permission,
    String] with BaseTypeMapper[Permission]
  {
    def map(e: Permission) = e match {
      case NormalUser => "normal"
      case Administrator => "admin"
    }

    def comap(s: String) = s match {
      case "normal" => NormalUser
      case "admin" => Administrator
    }

    override def sqlTypeName = Some("varchar(7)")
  }

  def id = column[Long]("id", O PrimaryKey, O AutoInc)

  def username = column[String]("username")

  def name = column[String]("name")

  def password = column[String]("password")

  def email = column[String]("email")

  def domain = column[String]("domain")

  def permission = column[Permission]("permission")

  def activated = column[Boolean]("active", O Default (false))

  def * = id ~ username ~ password ~ email ~ name ~ domain ~ permission ~ activated <>(Artist.apply _, Artist.unapply _)

  def noID = username ~ password ~ email ~ name ~ domain ~ permission


  def create(username: String, password: String, email: String, name: String): Int =
  {

    db.withSession {
      implicit s =>
        val id = Artists.noID insert(username, password, email, name, "", NormalUser)
        Logger.debug(Artists.insertStatement)
        id

    }
  }

  def updateDomain(artistId: Long, domain: String): Boolean =
  {
    db withSession {
      implicit s =>
        val inserted = (for {a <- Artists if a.id === artistId} yield a.domain ~ a.activated) update(domain, true)
        inserted == 1


    }
  }

  def domainAvail(domain: String): Boolean =
  {
    db.withSession {
      implicit s =>

        val total = for {
          a <- Artists if a.domain === domain.bind
        } yield a.domain.count
        total == 0
    }

  }

  def findByDomain(domain: String): Option[Artist] =
  {
    db.withSession {
      implicit s =>
        (
          for {
            a <- Artists if a.domain === domain.bind
          } yield a
          ).firstOption
    }


  }

  def authenticate(name: String, password: String): Option[Artist] =
  {
    findByUsername(name).filter {
      artist => artist.password == hash(password)
    }

  }

  def findByUsername(username: String): Option[Artist] =
  {
    db withSession {
      implicit s =>
        (for {a <- Artists if a.username === username.bind} yield a).firstOption
    }
  }

  def findByName(name: String): Option[Artist] =
  {
    db withSession {
      implicit s =>
        (for {a <- Artists if a.name === name.bind} yield a).firstOption
    }
  }

  private def hash(pass: String): String = pass

  def findByEmail(email: String): Option[Artist] =
  {
    db withSession {
      implicit s =>
        (for {a <- Artists if a.email === email.bind} yield a).firstOption
    }
  }

  def findById(id: Long): Option[Artist] =
  {
    db withSession {
      implicit s =>
        (for {a <- Artists if a.id === id} yield a).firstOption
    }
  }

  def findAll: Seq[Artist] =
  {
    db withSession {
      implicit s =>
        (for {a <- Artists} yield a).list
    }
  }

  def insertTags(artistID: Long, tags: List[String]): Int =
  {

    db withSession {
      implicit s =>
        val foundTags = Tags.find(tags)
        val artistTags = foundTags.map({
          tag => ArtistTag(artistID, tag.id)
        })
        if (!artistTags.isEmpty) {
          ArtistTags.insertAll(artistTags: _*)
        }

        val flattenTags = foundTags.map(_.name)
        val missingTags = tags.filter({
          tag => !flattenTags.contains(tag)
        })
        if (!missingTags.isEmpty) {
          Tags.noID insertAll (missingTags: _*)
        }


        val inserted = ArtistTags.flat insert (
          for {
            t <- Query(Tags).where(_.name.inSet(missingTags))
          } yield artistID ~ t.id

          )
        Logger.debug(ArtistTags.insertStatement)
        inserted

    }


  }

  private def insertArtistTags(tags: List[ArtistTag]) =
  {
    /* db withSession {
      implicit session =>
        ArtistTags.insertAll(artistTags)
    }*/
  }

  val random = new SecureRandom();


  /* def create(name: String, password: String, email: String, domain: String): Artist =
{
  val bytes: Array[Byte] = new Array[Byte](20)
  random.nextBytes(bytes)
  val salt = bytes.toString()

  val pass = hash(password, salt)
  DB.withConnection {
   implicit connection =>
     val id: Long = SQL("INSERT INTO artist VALUES ({email}, {pass}, {name}, {permission},{domain})").on(
       'domain -> domain,
       'email -> email,
       'salt -> salt,
       'pass -> pass,
       'name -> name,
       'permission -> NormalUser.toString
     ).executeInsert()

     new Artist(id, name, pass, email, NormalUser.toString, domain)
 }
  Artist
}  */


}