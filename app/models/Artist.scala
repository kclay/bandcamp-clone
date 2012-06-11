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


// Import the query language

import org.scalaquery.ql._

// Import the standard SQL types

import org.scalaquery.ql.TypeMapper._

// Use H2Driver which implements ExtendedProfile and thus requires ExtendedTables

import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}



case class Account(id: Long = 0, name: String, password: String, email: String, domain: String, permission: Permission = NormalUser)


/*object Account$ extends Table[(Long,String,String,String,Option[Permission],String]{

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

object Accounts extends Table[Account]("accounts") with DataTable
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

  def name = column[String]("name")

  def password = column[String]("password")

  def email = column[String]("email")

  def domain = column[String]("domain")

  def permission = column[Permission]("permission")

  def * = id ~ name ~ password ~ email ~ domain ~ permission <>(Account.apply _, Account.unapply _)

  def create(username: String, password: String, email: String, name: String): Int =
  {

    db.withSession {
      implicit s=>
      Accounts.insert(Account(0, username, password, email, name))

    }
  }

  def findByDomain(domain: String): Option[Account] =
  {
    db.withSession {
      implicit s =>
        (
          for {
            a <- Accounts if a.domain === domain.bind
          } yield a
          ).firstOption
    }


  }

  def authenticate(name: String, password: String): Option[Account] =
  {
    findByName(name).filter {
      artist => artist.password == hash(password)
    }
  }

  def findByName(name: String): Option[Account] =
  {
    db withSession {
      implicit s =>
        (for {a <- Accounts if a.name === name.bind} yield a).firstOption
    }
  }

  private def hash(pass: String): String = pass

  def findByEmail(email: String): Option[Account] =
  {
    db withSession {
      implicit s =>
        (for {a <- Accounts if a.email === email.bind} yield a).firstOption
    }
  }

  def findById(id: Long): Option[Account] =
  {
    db withSession {
      implicit s =>
        (for {a <- Accounts if a.id === id} yield a).firstOption
    }
  }

  def findAll: Seq[Account] =
  {
    db withSession {
      implicit s =>
        (for {a <- Accounts} yield a).list
    }
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