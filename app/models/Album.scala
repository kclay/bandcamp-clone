package models

import java.sql.Date
import utils.Image
import org.squeryl.PrimitiveTypeMode._
import scala.Some
import org.squeryl.KeyedEntity
import org.squeryl.annotations._
import scala.Some
import org.apache.commons.codec.digest.DigestUtils.shaHex

case class Album(var artistID: Long, session: String, name: String, artistName: Option[String], slug: String, active: Boolean = false,
                 download: Boolean = true, donateMore: Boolean = true, price: Double = 1.00,
                 art: Option[String], about: Option[String], credits: Option[String], upc: Option[String], releaseDate: Option[Date]) extends KeyedEntity[Long] {
  var id: Long = 0

  def this() = this(0, shaHex(String.valueOf(System.nanoTime())), "", Some(""), "", false, true, true, 1.00, Some(""), Some(""), Some(""), Some(""), Some(new Date(System.currentTimeMillis)))

  lazy val artURL: String = art.map(Image(_).url).getOrElse("")


}

object Album {

  import SiteDB._

  def apply() = new Album()

  def bySlug(artistId: Long, slug: String) =
    from(albums)(a =>
      where(a.artistID === artistId and a.slug === slug)
        select (a)
    ).headOption

  def validateSlug(artistId: Long, slug: String) =
    bySlug(artistId, slug).map(a => true).getOrElse(false)

  def forArtist(artistId: Long, albumId: Long): Option[Album] = {
    from(albums)(a =>
      where(a.id === albumId and a.artistID === artistId)
        select (a)
    ).headOption
  }

  def withTracks(albumID: Long): List[Option[Track]] =
    join(albumTracks, tracks.leftOuter)((at, t) =>
      where(at.albumID === albumID)
        select (t)
        on (at.trackID === t.map(_.id))

    ).iterator.toList
}


case class AlbumTracks(@Column("album_id") albumID: Long, @Column("track_id") trackID: Long, order: Int) {


}

case class Track(var id: Long = 0, var artistID: Long, name: String, donateMore: Boolean = true, download: Boolean = true, price: Double = 1.00,
                 license: String, artistName: Option[String],
                 art: Option[String], lyrics: Option[String], about: Option[String], credits: Option[String], releaseDate: Option[Date], active: Boolean = false) extends KeyedEntity[Long] {
  def this() = this(0, 0, "", true, true, 1.00, "", Some(""), Some(""), Some(""), Some(""), Some(""), Some(new Date(System.currentTimeMillis)), false)


}

object Track {

  import SiteDB._

  def find(id: Long): Option[Track] = tracks.where(t => t.id === id).headOption

  def publish(id: Long) = update(tracks)(t =>
    where(t.id === id)
      set (t.active := true)
  )
}


case class Genre(name: String) extends KeyedEntity[Long] {
  var id: Long = 0
}

object Genre {

  import SiteDB._

  def allAsString: List[(String, String)] = inTransaction(from(genres)(g => select(g.id.toString, g.name)).toList)

  def all: List[Genre] = from(genres)(g => select(g)).toList
}


