package models

import org.squeryl.KeyedEntity
import java.sql.{Timestamp, Date}

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 8/7/12
 * Time: 1:43 PM
 */

sealed trait Metric

case object Play extends Metric

case object Complete extends Metric

case object Partial extends Metric

case object Skip extends Metric

case object Downloads extends Metric

case object Purchase extends Metric


case class Stat(artistID: Long, metric: String, trackedAt: Date, objectID: Long, total: Int) extends KeyedEntity[Long] {
  var id: Long = 0

  def this() = this(0, "", new Date(System.currentTimeMillis()), 0, 0)
}

object Stat {

  def apply(metric: Metric, artistID: Long, objectID: Long) = {
    new Stat(artistID, metric.toString.toLowerCase, new Date(System.currentTimeMillis()), objectID, 1)
  }

}
