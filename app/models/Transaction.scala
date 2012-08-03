package models

import java.text.SimpleDateFormat

import java.sql.Timestamp
import org.squeryl.KeyedEntity
import org.apache.commons.codec.digest.DigestUtils._
import scala.Some
import services.PayPal

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 7/26/12
 * Time: 12:22 AM
 */


case class Transaction(itemID: Long, amount: Double, kind: String, token: String, status: String = "pending", correlationID: Option[String] = Some(""), payerID: Option[String] = Some(""),
                       transactionID: Option[String] = Some(""), ack: String = "",
                       created: Option[Timestamp] = None) extends KeyedEntity[Long] {
  var id: Long = 0


  def this() = this(0, 0, "", "", "pending", Some(""), Some(""), Some(""), "", Some(new Timestamp(System.currentTimeMillis)))
}

object Transaction {
  val PURCHASE_TRACK = "track"
  val PURCHASE_ALBUM = "album"
  val STATUS_PENDING = "pending"
  val STATUS_CALLBACK = "callback"
  val STATUS_ERROR = "error"
  val STATUS_VOID = "void"
  val STATUS_COMPLETED = "completed"
  val STATUS_CHECKOUT = "checkout"

  lazy val timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  import models.SiteDB._
  import org.squeryl.PrimitiveTypeMode._

  def fromTimestamp(date: String) = timeFormatter format date

  def withItem(token:String)={
    byToken(token).map {
      t =>
        val item = if (t.kind == PURCHASE_ALBUM)
          albums.where(a => a.id === t.itemID).head
        else tracks.where(t => t.id === t.itemID).head

       Some(item)

    }.getOrElse(None)
  }
  def withArtistAndItem(token: String) = {
    byToken(token).map {
      t =>
        val item = if (t.kind == PURCHASE_ALBUM)
          albums.where(a => a.id === t.itemID).head
        else tracks.where(t => t.id === t.itemID).head

        Some((t, artists.where(a => a.id === item.ownerID).head, item))

    }.getOrElse(None)

  }

  def byToken(token: String) = transactions.where(t => t.token === token).headOption


  def status(token: String, status: String) = {
    update(transactions)(t =>
      where(t.token === token)
        set (t.status := status)
    )
  }

  def commit(token: String, correlationID: Option[String], transactionID: Option[String]) = {
    update(transactions)(t =>
      where(t.token === token)
        set(t.status := STATUS_COMPLETED,
        t.correlationID := correlationID,
        t.transactionID := transactionID)
    )
  }

  def apply(item: SaleAbleItem, amount: Double, token: String) = inTransaction {
    transactions.insert(new Transaction(item.itemID, amount, item.itemType, token))
  }


}
