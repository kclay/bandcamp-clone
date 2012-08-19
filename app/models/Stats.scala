package models

import org.squeryl.{Table, Query, KeyedEntity}
import java.sql.{Timestamp, Date}
import play.api.Logger
import org.joda.time.{DateTimeZone, DateTime, LocalDate}
import models.SiteDB._
import org.squeryl.PrimitiveTypeMode._


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 8/7/12
 * Time: 1:43 PM
 */


trait Range {

  val name = ""

  def unapply(value: String) = if (value.equals(name)) Some(this) else None

  def mapTracks(query: Query[(Stat, Long, String, String)]) = {
    // TODO : Optomize the query
    Map("items" -> query.map {
      case (s, id, name, slug) => id -> Map("name" -> name, "slug" -> slug)

    }.toMap,
      "stats" -> query.groupBy {
        case (s, _, _, _) => s.trackedAt

      }.mapValues(v => v.map {
        case (s, _, _, _) => s
      })

    )
  }

  def mapSales(query: Query[(Stat, Option[DoubleType], Long, String, String)]) = {
    // TODO : Optomize the query
    Map("items" -> query.map {
      case (s, usd, id, name, slug) => id -> Map("name" -> name, "slug" -> slug, "usd" -> usd)

    }.toMap,
      "stats" -> query.groupBy {
        case (s, _, _, _, _) => s.trackedAt

      }.mapValues(v => v.map {
        case (s, _, _, _, _) => s
      })

    )
  }


  def ~(metric: TrackMetric)(implicit artist: Artist) = {
    mapTracks(metric.query)
  }

  def ~(metric: PurchaseMetric)(implicit artist: Artist) = {
    mapSales(metric.query)
  }
}


trait Restricted extends Range {
  val range = DateTime.now(DateTimeZone.UTC)

  def withTracks(query: Query[(Stat, Long, String, String)]) = query.where(
    s => s._1.trackedAt gte new Date(range.getMillis)
  )

  def withSales(query: Query[(Stat, Option[DoubleType], Long, String, String)]) = query.where(

    s => s._1.trackedAt gte new Date(range.getMillis)
  )

  override def ~(metric: TrackMetric)(implicit artist: Artist) = {
    mapTracks(withTracks(metric.query))
  }

  override def ~(metric: PurchaseMetric)(implicit artist: Artist) = {
    mapSales(withSales(metric.query))
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


  val name = ""

  val computeSales = false


  def unapply(value: String) = if (value.equals(name)) Some(this) else None


}

trait TrackMetric extends Metric {
  def query(implicit artist: Artist) = join(stats, tracks)((s, t) =>
    where(s.artistID === artist.id and s.metric === name)
      select(s, t.id, t.name, t.slug)
      orderBy (s.trackedAt asc)
      on (s.objectID === t.id)

  )

   def ~(range: Range)(implicit artist: Artist): Any = {
    range ~ this
  }


}

case object Play extends TrackMetric {
  override val name = "play"
}

case object InvalidMetric extends TrackMetric {
  override val name = "null"
}

case object Complete extends TrackMetric {
  override val name = "complete"
}

case object Partial extends TrackMetric {
  override val name = "partial"
}

case object Skip extends TrackMetric {
  override val name = "skip"
}

case object TrackDownload extends Metric {
  override val name = "track_downloads"
}

case object AlbumDownload extends Metric {
  override val name = "album_downloads"
}


trait PurchaseMetric extends Metric {
  val table: Table[SaleAbleItem]
  override val computeSales = true

  def cut(implicit artist: Artist) = from(SiteDB.sales)(s =>
    where(s.artistID === artist.id)
      groupBy(s.itemID, s.createdAt)
      compute (sum(s.amount))

  )

  def sales(implicit artist: Artist) = join(stats, SiteDB.sales)((st, s) =>
    where(st.artistID === artist.id and st.metric === name)
      select(st, s)
      on (st.objectID === s.id)
  )

  def query(implicit artist: Artist) = from(sales, cut, table)((s, sums, i) =>
    where(sums.key._1 === s._1.objectID and s._2.itemID === i.itemID)
      // select stats
      select(s._1, sums.measures, i.itemID, i.itemTitle, i.itemSlug)

  )

  def ~(range: Range)(implicit artist: Artist): Any = {
    range ~ this
  }


}

case object PurchaseTrack extends PurchaseMetric {
  override val name = "purchase_track"

  override val table = tracks.asInstanceOf[Table[SaleAbleItem]]


}

case object PurchaseAlbum extends PurchaseMetric {
  override val name = "purchase_album"

  override val table = albums.asInstanceOf[Table[SaleAbleItem]]


}

case object Sales extends Metric {
  override val name = "sales"
}


case class Stat(artistID: Long, metric: String, trackedAt: Date, objectID: Long, total: Int) {


  def this() = this(0, "", new Date(DateTime.now(DateTimeZone.UTC).getMillis), 0, 0)
}

object Stat {


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
