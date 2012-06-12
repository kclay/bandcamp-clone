package models

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/11/12
 * Time: 11:06 AM
 */

import play.api.db._
import play.api.Play.current
import play.api.Logger

// Import the session management, including the implicit threadLocalSession

import org.scalaquery.session._


// Import the query language

import org.scalaquery.ql._

// Import the standard SQL types

import org.scalaquery.ql.TypeMapper._

// Use H2Driver which implements ExtendedProfile and thus requires ExtendedTables

import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

case class Tag(id: Int, name: String)

object Tags extends Table[Tag]("tags") with DataTable
{

  def id = column[Int]("id", O PrimaryKey, O AutoInc)

  def name = column[String]("name")

  def * = id ~ name <>(Tag.apply _, Tag.unapply _)

  def noID = name

  def asTuple = id ~ name

  def search(query: String): List[Tag] =
  {
    db withSession {
      implicit s =>

        Tags.where(_.name like "%" + query.trim + "%") list


    }

  }

  def find(query: List[String]): List[Tag] =
  {
    db withSession {
      implicit s =>
        Tags.where(_.name.inSet(query.map(_.trim))).list
    }

  }

}