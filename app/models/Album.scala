package models

import java.sql.Date
import utils.{Small, Image, Default, Medium}
import org.squeryl.PrimitiveTypeMode._
import scala.Some
import scala.math
import org.squeryl.KeyedEntity
import org.squeryl.annotations._
import scala.Some
import org.apache.commons.codec.digest.DigestUtils.shaHex
import utils.Assets._

trait SaleAbleItem {
  def itemType: String


  def itemID: Long

  def ownerID: Long

  def itemTitle: String

  def signature: String

  def itemSlug: String

  def smallArtURL: String

  def itemArtistName: Option[String]

  def artist = Artist.find(ownerID)
}

case class Album(var id: Long = 0, var artistID: Long, session: String, name: String, artistName: Option[String], var slug: String, var active: Boolean = false,
                 download: Boolean = true, donateMore: Boolean = true, price: Double = 1.00,
                 art: Option[String], about: Option[String], credits: Option[String], upc: Option[String], releaseDate: Option[Date]) extends KeyedEntity[Long] with SaleAbleItem {


  def this() = this(0, 0, shaHex(String.valueOf(System.nanoTime())), "", Some(""), "", false, true, true, 1.00, Some(""), Some(""), Some(""), Some(""), Some(new Date(System.currentTimeMillis)))

  def artImage = art.map(a => Some(Image(a))).getOrElse(None)


  def smallArtImage = artImage.map(a => Some(a.getOrResize(Small()))).getOrElse(None)

  def smallArtURL = smallArtImage.map(_.url).getOrElse("")

  def defaultArtImage = artImage.map(a => Some(a.getOrResize(Default()))).getOrElse(None)

  lazy val artURL: String = art.map(Image(_).getOrResize(Default()).url).getOrElse("")

  def itemArtistName: Option[String] = artistName

  def itemType = "album"

  def itemID = id

  def itemTitle = name

  def ownerID: Long = artistID

  def signature: String = session

  def itemSlug: String = slug


  @Transient
  lazy val tags = {
    import models.Tag.albumTagCreator
    Tag.forItem(this)(albumTagCreator)
  }

  def rebuild() = {
    artImage.map {
      a =>
        a.getOrResize(Default(), true)
        a.getOrResize(Small(), true)
        a.getOrResize(Medium(), true)
    }
  }

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

  def list(page: Int = 1, amount: Int = 20, orderAsc: Boolean = false) = {
    join(albums, artists)((a, aa) =>
      where(a.active === true)
        select(a, aa)
        orderBy (if (orderAsc) a.id.asc else a.id.desc)
        on (a.artistID === aa.id)
    ).take(amount).drop(page - 1 * amount).toList
  }

  def bySession(artistId: Long, session: String) = inTransaction(from(albums)(a =>
    where(a.artistID === artistId and a.session === session)
      select (a)
  ).headOption)

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
    join(albumTracks, tracks)((at, t) =>
      where(at.albumID === albumID)
        select (t)
        orderBy (at.order asc)
        on (at.trackID === t.id)


    )
}


case class AlbumTracks(@Column("album_id") albumID: Long, @Column("track_id") trackID: Long, order: Int) {


}

object AlbumTracks {

  import SiteDB._

  def withAlbum(trackID: Long) =
    join(albumTracks, albums)((at, a) =>
      where(at.trackID === trackID)
        select (a)

        on (at.albumID === a.id)


    ).headOption


}

case class Track(var id: Long = 0, var artistID: Long, session: String, file: Option[String], fileName: Option[String],
                 name: String, var slug: String, donateMore: Boolean = true, download: Boolean = true, price: Double = 1.00,
                 artistName: Option[String],
                 art: Option[String], lyrics: Option[String], about: Option[String], credits: Option[String], releaseDate: Option[Date], active: Boolean = false, var duration: Int = 0)
  extends KeyedEntity[Long] with SaleAbleItem {
  def this() = this(0, 0, shaHex(String.valueOf(System.nanoTime())), Some(""), Some(""), "", "", true, true, 1.00, Some(""), Some(""), Some(""), Some(""), Some(""), Some(new Date(System.currentTimeMillis)), false, 0)

  var single = false

  def previewURL(host: String) = {
    file.map(audioStore.previewURL(host, session, _)).getOrElse("")

  }

  def withTime = "%02d:%02d".format(math.floor(duration / 60).toInt, math.floor(duration % 60).toInt)

  def rebuild = {
    artImage.map {
      a =>
        a.getOrResize(Default(), true)
        a.getOrResize(Small(), true)
        a.getOrResize(Medium(), true)
    }
  }

  def toFile = file.map(audioStore.full(session, _)).getOrElse(None)

  def itemType = "track"

  def itemID = id

  def itemTitle = name

  def ownerID: Long = artistID

  def itemSlug: String = slug


  def signature: String = file.get

  lazy val artURL: String = art.map(Image(_).getOrResize(Default()).url).getOrElse("")


  def artImage = art.map(a => Some(Image(a))).getOrElse(None)

  def defaultArtImage = artImage.map(a => Some(a.getOrResize(Default()))).getOrElse(None)

  def smallArtImage = artImage.map(a => Some(a.getOrResize(Small()))).getOrElse(None)

  def smallArtURL = smallArtImage.map(_.url).getOrElse("")

  def itemArtistName: Option[String] = artistName

  @Transient
  lazy val tags = {
    import models.Tag.trackTagCreator
    Tag.forItem(this)(trackTagCreator)

  }


}

object Track {

  import SiteDB._

  def apply() = new Track()

  def find(id: Long): Option[Track] = tracks.where(t => t.id === id).headOption

  def bySlug(artistId: Long, slug: String) = tracks.where(t => t.artistID === artistId and t.slug === slug).headOption

  def publish(id: Long) = update(tracks)(t =>
    where(t.id === id)
      set (t.active := true)
  )

  def withSingle(artistID: Long, page: Int = 1, amount: Int = 20) = from(tracks)(t =>
    where(t.artistID === artistID and t.single === true)
      select (t)
  ).take(amount).drop(page - 1 * amount).toList

  def withSingleAndArtist(page: Int = 1, amount: Int = 20, orderAsc: Boolean = false) = join(tracks, artists)((t, a) =>
    where(t.single === true and t.active === true)
      select(t, a)
      orderBy (if (orderAsc) t.id.asc else t.id.desc)
      on (t.artistID === a.id)
  ).take(amount).drop(page - 1 * amount).toList

  def byFile(artistID: Long, file: String): Option[Track] = inTransaction(tracks.where(t => t.artistID === artistID and t.file === Some(file)).headOption)
}


case class Genre(name: String) extends KeyedEntity[Long] {
  var id: Long = 0
}

object Genre {

  import SiteDB._

  def allAsString: List[(String, String)] = inTransaction(from(genres)(g => select(g.id.toString, g.name)).toList)

  def all: List[Genre] = from(genres)(g => select(g)).toList
}


