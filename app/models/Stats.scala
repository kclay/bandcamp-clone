package models

import org.squeryl.KeyedEntity
import java.sql.{Timestamp, Date}
import play.api.Logger


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 8/7/12
 * Time: 1:43 PM
 */

sealed abstract class Metric {
  val name = ""

  def unapply(value: String) = if (value.equals(name)) Some(this) else None

}

case object Play extends Metric {
  override val name = "play"
}

case object Null extends Metric {
  override val name = "null"
}

case object Complete extends Metric {
  override val name = "complete"
}

case object Partial extends Metric {
  override val name = "partial"
}

case object Skip extends Metric {
  override val name = "skip"
}

case object Downloads extends Metric {
  override val name = "downloads"
}

case object PurchaseTrack extends Metric {
  override val name = "purchase_track"
}

case object PurchaseAlbum extends Metric {
  override val name = "purchase_album"
}


case class Stat(artistID: Long, metric: String, trackedAt: Date, objectID: Long, total: Int) {


  def this() = this(0, "", new Date(System.currentTimeMillis()), 0, 0)
}

object Stat {

  import models.SiteDB._
  import org.squeryl.PrimitiveTypeMode._

  type MetricType = Metric

  def today = new Date(System.currentTimeMillis())

  def remove(metric: Metric, artistID: Long, objectID: Long) = {

    update(stats)(s =>
      where(s.metric === metric.name and s.trackedAt === today)
        set (s.total := s.total.~ - 1)
    )
  }



  def apply(metric: Metric, artistID: Long, objectID: Long) = {
    try {
      new Stat(artistID, metric.name, today, objectID, 1).save
    } catch {
      case e: RuntimeException =>
        if (e.getMessage.contains("Duplicate")) {
          update(stats)(s =>
            where(s.artistID === artistID and s.metric === metric.name and s.trackedAt === today and s.objectID === objectID)
              set (s.total := s.total.~ + 1)
          )
        }

    }
  }


}
