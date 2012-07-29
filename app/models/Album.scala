package models

import java.sql.Date
import utils.{Small, Image}
import org.squeryl.PrimitiveTypeMode._
import scala.Some
import org.squeryl.KeyedEntity
import org.squeryl.annotations._
import scala.Some
import org.apache.commons.codec.digest.DigestUtils.shaHex

case class Album(var id: Long = 0, var artistID: Long, session: String, name: String, artistName: Option[String], slug: String, var active: Boolean = false,
                 download: Boolean = true, donateMore: Boolean = true, price: Double = 1.00,
                 art: Option[String], about: Option[String], credits: Option[String], upc: Option[String], releaseDate: Option[Date]) extends KeyedEntity[Long] {


  def this() = this(0, 0, shaHex(String.valueOf(System.nanoTime())), "", Some(""), "", false, true, true, 1.00, Some(""), Some(""), Some(""), Some(""), Some(new Date(System.currentTimeMillis)))

  def artImage = art.map(a => Some(Image(a))).getOrElse(None)

  //lazy val smallArt = artImage.map(a => Some(a.getOrResize(Small()))).getOrElse(None)
  lazy val artURL: String = art.map(Image(_).url).getOrElse("")


}

object Album {

  import SiteDB._

  def apply() = new Album()

  def bySlug(artistId: Long, slug: String) = inTransaction(
    from(albums)(a =>
      where(a.artistID === artistId and a.slug === slug)
        select (a)
    ).headOption
  )

  def validateSlug(artistId: Long, slug: String) =
    bySlug(artistId, slug).map(a => true).getOrElse(false)

  def forArtist(artistId: Long, page: Int = 1, amount: Int = 20) = {
    from(albums)(a =>
      where(a.artistID === artistId)
        select (a)
    ).take(amount).drop(page - 1 * amount).toList
  }

  def forArtist(artistId: Long, albumId: Long): Option[Album] = {
    from(albums)(a =>
      where(a.id === albumId and a.artistID === artistId)
        select (a)
    ).headOption
  }

  def withTracks(albumID: Long) =
    join(albumTracks, tracks.leftOuter)((at, t) =>
      where(at.albumID === albumID)
        select (t)
        orderBy (at.order asc)
        on (at.trackID === t.map(_.id))


    )
}


case class AlbumTracks(@Column("album_id") albumID: Long, @Column("track_id") trackID: Long, order: Int) {


}

case class Track(var id: Long = 0, var artistID: Long, session: String, file: Option[String], name: String, var slug: String, donateMore: Boolean = true, download: Boolean = true, price: Double = 1.00,
                 license: String, artistName: Option[String],
                 art: Option[String], lyrics: Option[String], about: Option[String], credits: Option[String], releaseDate: Option[Date], active: Boolean = false) extends KeyedEntity[Long] {
  def this() = this(0, 0, "", Some(""), "", "", true, true, 1.00, "", Some(""), Some(""), Some(""), Some(""), Some(""), Some(new Date(System.currentTimeMillis)), false)


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


