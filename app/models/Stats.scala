package models

import org.squeryl.{Query, KeyedEntity}
import java.sql.{Timestamp, Date}
import play.api.Logger
import org.joda.time.{DateTimeZone, DateTime, LocalDate}
import org.squeryl.PrimitiveTypeMode._
import scala.Some


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 8/7/12
 * Time: 1:43 PM
 */


trait Range {

  val name = ""

  def unapply(value: String) = if (value.equals(name)) Some(this) else None

  def toMap(query: Query[(Stat, Track)]) = {

      Map("items" -> query.map {
        r => r._2.id -> Map("name" -> r._2.name, "slug" -> r._2.slug)
      }.toMap,
        "stats" -> query.groupBy {
          r => r._1.trackedAt
        }.mapValues(v => v.map {
          case (r) => r._1
        })

      )
  }


  def compute(metric: Metric) = {
    toMap(metric.query)
  }
}


trait Restricted extends Range {
  val range = DateTime.now(DateTimeZone.UTC)

  def prep(query: Query[(Stat, Track)]) = from(query)(s =>
    where(s._1.trackedAt gte new Date(range.getMillis))
      select (s)
  )

  override def compute(metric: Metric) = {
    toMap(prep(metric.query))
  }
}

case object Today extends Restricted {
  override val name = "today"

}

case object Week extends Restricted {
  override val name = "week"
  override val range = DateTime.now(DateTimeZone.UTC).minusDays(7)


}

case object Month extends Restricted {
  override val name = "month"
  override val range = DateTime.now(DateTimeZone.UTC).minusDays(30)
}

case object AllTime extends Range {
  override val name = "alltime"

}

case object TwoMonths extends Restricted {
  override val name = "twomonths"
  override val range = DateTime.now(DateTimeZone.UTC).minusDays(60)
}

case object InvalidRange extends Range {
  override val name = "invalid"
}


sealed abstract class Metric {


  import models.SiteDB._

  val name = ""


  def unapply(value: String) = if (value.equals(name)) Some(this) else None

  def query = join(stats, tracks)((s, t) =>
    select(s, t).
      orderBy(s.trackedAt asc)
      on (s.objectID === t.id)

  )


}

case object Play extends Metric {
  override val name = "play"
}

case object InvalidMetric extends Metric {
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

case object TrackDownload extends Metric {
  override val name = "track_downloads"
}

case object AlbumDownload extends Metric {
  override val name = "album_downloads"
}

case object PurchaseTrack extends Metric {
  override val name = "purchase_track"
}

case object PurchaseAlbum extends Metric {
  override val name = "purchase_album"
}

case object Sales extends Metric {
  override val name = "sales"
}


case class Stat(artistID: Long, metric: String, trackedAt: Date, objectID: Long, total: Int) {


  def this() = this(0, "", new Date(DateTime.now(DateTimeZone.UTC).getMillis), 0, 0)
}

object Stat {

  import models.SiteDB._
  import org.squeryl.PrimitiveTypeMode._

  type MetricType = Metric

  // track everything as utc
  def today = new Date(DateTime.now(DateTimeZone.UTC).getMillis)

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
