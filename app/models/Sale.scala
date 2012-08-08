package models

import java.sql.Timestamp
import org.squeryl.KeyedEntity

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 8/7/12
 * Time: 1:59 PM
 */


case class Sale(transactionID: Long, downloads: Int, amount: Double, createdAt: Timestamp) extends KeyedEntity[Long] {
  val id: Long = 0

  def this() = this(0, 0, 0, new Timestamp(System.currentTimeMillis()))
}

object Sale {

  def apply(transaction: Transaction, directPayment: Boolean = true)=None
}
