package models

import utils.{AudioDataStore}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.{KeyedEntity, Schema}
import org.squeryl.annotations.Column


import scala.Some


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/20/12
 * Time: 7:30 PM
 */

class DBObject extends KeyedEntity[Long] {
  var id: Long = 0
}

case class Tag(name: String) extends DBObject

object Tag {

  import SiteDB._

  def byName(s: List[String]) = tags.where(t => t.name in s)

  def search(query: String): List[Tag] = tags.where(t => t.name like "%" + query + "%").toList

  def find(query: List[String]): List[Tag] = byName(query).toList
}


case class Queue(file: String, session: String, status: String = "new", started: Option[Long], ended: Option[Long], duration: Int) extends DBObject {

  def this() = this("", "", "", Some(0L), Some(0L), 0)


}

object Queue {

  import SiteDB._

  val STATUS_NEW = "new"
  val STATUS_PROCESSING = "processing"
  val STATUS_ERROR_PREVIEW = "error_encode_preview"
  val STATUS_ERROR_FULL = "error_encode_full"
  val STATUS_COMPLETED = "completed"
  lazy val audioDataStore = new AudioDataStore()

  def status(ids: List[Long]) = inTransaction {
    from(queue)(q =>
      where(q.id in ids)
        select(q.id, q.status, q.duration)
    ).toList
  }

  def updateStatus(id: Long, status: String) = inTransaction {
    update(queue)(q => where(q.id === id)
      set (q.status := status)
    )

  }

  def updateStatus(id: Long, status: String, duration: Int) = inTransaction {
    update(queue)(q => where(q.id === id)
      set(q.status := status,
      q.duration := duration)
    )

  }

  def fetch(id: Long) = inTransaction {
    queue.where(q => q.id === id).headOption
  }

  def delete(queue: Queue): Boolean = inTransaction {
    delete(queue.id)
  }

  def delete(queueId: Long): Boolean = inTransaction {
    queue.delete(queueId)
  }


  /* def forName(name: String) = inTransaction {
    queue.where(q => q.name === name)
  }*/

  def add(name: String, folder: String) = inTransaction {
    queue.insert(Queue(name, folder)).id
  }

  def apply(name: String, folder: String) = new Queue(name, folder, "new", Some(0L), Some(0L), 0)
}

case class Encoding(val id: Long, @Column("track_id") trackID: Long)


object SiteDB extends Schema {

  val queue = table[Queue]("queue")
  on(queue)(q => declare(
    q.started is (indexed)
  ))
  val artists = table[Artist]("artists")
  on(artists)(a => declare(
    a.name is (named("artist_name"))

  ))
  val albums = table[Album]("albums")
  on(albums)(a => declare(
    a.name is (named("album_name")),
    a.active is (indexed),
    a.artistID is(indexed, named("artist_id")),
    a.artistName is (dbType("varchar(45)")),
    a.art is (dbType("varchar(45)")),
    a.about is (dbType("text")),
    a.credits is (dbType("text")),
    a.upc is (dbType("varchar(20)"))
  ))
  val tracks = table[Track]("tracks")
  on(tracks)(t => declare(
    t.name is (named("track_name")),
    t.active is (indexed),
    t.artistID is(indexed, named("artist_id")),
    t.artistName is (dbType("varchar(45)")),
    t.art is (dbType("varchar(45)")),
    /*t.about is (dbType("text")),*/
    t.credits is (dbType("text")),
    t.lyrics is (dbType("text"))
  ))
  val albumTracks = table[AlbumTracks]("album_tracks")
  on(albumTracks)(at => declare(
    at.albumID is (named("album_id")),
    at.trackID is (named("track_id")),
    columns(at.albumID, at.trackID) are(primaryKey, unique),
    at.order is (named("track_order")),
    at.order defaultsTo (0)

  ))

  val artistTags = table[ArtistTag]("artist_tags")
  on(artistTags)(at => declare(
    at.artistID is (named("artist_id")),
    at.tagID is (named("tag_id"))

  ))
  val tags = table[Tag]("tags")

  on(tags)(t => declare(
    t.name is(indexed, named("tag_name"))
  ))

  val genres = table[Genre]
  on(genres)(g => declare(
    g.name is (named("genre_name"))
  )
  )
  val transactions = table[Transaction]("transactions")
  on(transactions)(t => declare(
    t.token is (named("paypal_token")),
    t.correlationID is (named("correlation_id")),
    t.transactionID is (named("transaction_id")),
    t.payerID is (named("payer_id")),
    t.created is (named("created_at")),
    t.itemID is (named("item_id"))
  ))

  val stats = table[Stat]("stats")
  on(stats)(s => declare(
    s.artistID is (named("artist_id")),
    s.objectID is (named("object_id")),
    s.trackedAt is (named("tracked_at"))
  ))
  val sales = table[Sale]("sales")
  on(sales)(s => declare(
    s.transactionID is (named("transaction_id")),
    s.createdAt is (named("created_at"))
  ))
  val resets = table[PasswordReset]("password_resets")
  on(resets)(r => declare(

    r.artistID is (named("artist_id")),
    r.createdAt is (named("created_at"))
  ))


}



