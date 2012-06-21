package models

import org.scalaquery.session.Database
import play.api.db.DB
import play.api.Play.current
import org.scalaquery.ql.extended.AbstractExtendedTable
import java.sql.Date
import org.scalaquery.ql.basic.AbstractBasicTable


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/8/12
 * Time: 7:03 PM
 */

abstract class AbstractDataTable

trait DataTable
{
  lazy val db = Database.forDataSource(DB.getDataSource())


}

trait DataStore[T]
{
  self: AbstractExtendedTable[T] with DataTable =>



}

trait BasicDataTable[T] extends DataTable
{
  self: AbstractExtendedTable[T] =>

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name", O.NotNull)

  def about = column[Option[String]]("about", O.Nullable, O DBType ("text"))

  def price = column[Option[Double]]("price")

  def credits = column[Option[String]]("about", O.Nullable, O DBType ("text"))

  def artistName = column[Option[String]]("artistName", O.Nullable, O DBType ("varchar(45)"))

  def art = column[Option[String]]("art", O.Nullable, O DBType ("varchar(45)"))

  def releaseDate = column[Option[Date]]("releaseDate", O Nullable)

}
