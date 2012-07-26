package models

import java.text.SimpleDateFormat

import java.sql.Timestamp

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 7/26/12
 * Time: 12:22 AM
 */


case class Transaction(id: Long, amount: Double, kind: String, token: String, status: String = "pending", correlationId: String = "", payerID: String = "", ack: String = "",
                       created: Option[Timestamp]) {
  def this() = this(0, 0, "", "", "pending", "", "", "", Some(new Timestamp(System.currentTimeMillis)))
}

object Transaction {
  val PURCHASE_TRACK = "track"
  val PURCHASE_ALBUM = "album"
  lazy val timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def fromTimestamp(date: String) = timeFormatter format date
}
