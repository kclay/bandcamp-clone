package models

import java.sql.Date
import utils._
import org.squeryl.PrimitiveTypeMode._
import scala.math
import org.squeryl.KeyedEntity
import org.squeryl.annotations._
import org.apache.commons.codec.digest.DigestUtils.shaHex
import utils.Assets._
import scala.Some
import utils.Medium
import scala.Some
import utils.{BaseImage, Default, Small, Medium, Image}
import play.api.cache.Cache
import org.codehaus.jackson.annotate.JsonProperty


/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

trait SaleAbleItem {
  def itemType: String


  def itemID: Long

  def ownerID: Long

  def itemTitle: String

  def artImage: Image

  def signature: String

  def itemSlug: String

  def smallArtURL: String

  def mediumArtURL: String

  def itemArtistName: Option[String]

  def artist = Artist.find(ownerID)

  def url(host: String): String = Option(host)
    .map(h => if (h.startsWith("http://")) h else ("http://" + h))
    .get + "/" + itemType + "/" + itemSlug
}

case class Album(var id: Long = 0, var artistID: Long, session: String, name: String, artistName: Option[String], var slug: String, var active: Boolean = false,
                 download: Boolean = true, donateMore: Boolean = true, price: Double = 1.00,
                 art: Option[String], about: Option[String], credits: Option[String], upc: Option[String], releaseDate: Option[Date]) extends KeyedEntity[Long] with SaleAbleItem {


  def this() = this(0, 0, shaHex(String.valueOf(System.nanoTime())), "", Some(""), "", false, true, true, 1.00, Some(""), Some(""), Some(""), Some(""), Some(new Date(System.currentTimeMillis)))

  def artImage = art.map(a => new BaseImage(a)).getOrElse(new DefaultCoverImage())


  def smallArtImage = artImage.getOrResize(Small())

  def smallArtURL = smallArtImage.url

  def mediumArtURL = artImage.getOrResize(Medium()).url

  def defaultArtImage = artImage.getOrResize(Default())

  @Transient
  lazy val artURL: String = artImage.url

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
    artImage.getOrResize(Default(), true)
    artImage.getOrResize(Small(), true)
    artImage.getOrResize(Medium(), true)

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

trait BaseTrack extends KeyedEntity[Long] with SaleAbleItem {


  var artistID: Long
  val session: String
  val file: Option[String]
  val fileName: Option[String]
  val name: String
  var slug: String
  val download: Boolean = true
  val price: Double = 1.00
  val artistName: Option[String]
  val art: Option[String]
  val lyrics: Option[String]
  val about: Option[String]
  val credits: Option[String]
  val releaseDate: Option[Date]
  val active: Boolean = false

  val genreID: Long = 0


  def genre = Utils.genreByID(genreID)

  def previewURL(host: String): String = {
    file.map(audioStore.previewURL(host, session, _)).getOrElse("")

  }

  def trackDuration = 0

  def withTime = "%02d:%02d".format(math.floor(trackDuration / 60).toInt, math.floor(trackDuration % 60).toInt)

  def rebuild = {
    artImage.getOrResize(Default(), true)
    artImage.getOrResize(Small(), true)
    artImage.getOrResize(Medium(), true)

  }

  def toFile = file.map(audioStore.full(session, _)).getOrElse(None)

  def itemType = "track"

  def itemID = id

  def itemTitle = name

  def ownerID: Long = artistID

  def itemSlug: String = slug


  def signature: String = file.get

  @Transient
  lazy val artURL: String = artImage.url


  def artImage = art.map(a => new BaseImage(a)).getOrElse(new DefaultCoverImage())

  def defaultArtImage = artImage.getOrResize(Default())

  def mediumArtURL = artImage.getOrResize(Medium()).url

  def smallArtImage = artImage.getOrResize(Small()) //map(a => Some(a.getOrResize(Small()))).getOrElse(None)

  def smallArtURL = smallArtImage.url

  def itemArtistName: Option[String] = artistName
}

case class Track(var id: Long = 0, var artistID: Long, session: String, file: Option[String], fileName: Option[String],
                name: String, var slug: String,   override val download: Boolean = true, override val price: Double = 1.00,
                 @JsonProperty("artist")
                 artistName: Option[String],
                 art: Option[String], lyrics: Option[String], about: Option[String], credits: Option[String], releaseDate: Option[Date],
                 override val active: Boolean = false, var duration: Int = 0,
                 @JsonProperty("genre")
                 override val genreID: Long = 0) extends BaseTrack {
  def this() = this(0, 0, shaHex(String.valueOf(System.nanoTime())), Some(""), Some(""), "", "", true, 1.00, Some(""), Some(""), Some(""), Some(""), Some(""), Some(new Date(System.currentTimeMillis)), false, 0)

  var single = false


  @Transient
  lazy val tags = {
    import models.Tag.trackTagCreator
    Tag.forItem(this)(trackTagCreator)

  }


}


case class TrackWithTags(var id: Long = 0, var artistID: Long, session: String,  file: Option[String],  fileName: Option[String],
                         name: String,  var slug: String,    override val download: Boolean = true,    override val price: Double = 1.00,

                         artistName: Option[String],
                         art: Option[String],  lyrics: Option[String],  about: Option[String],  credits: Option[String], releaseDate: Option[Date],
                         override val active: Boolean = false, var duration: Int = 0,    override val genreID: Long = 0) extends BaseTrack {
  val tags: String = ""
  var single = false

  override def trackDuration = duration
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


case class Genre(name: String, tag: String) extends KeyedEntity[Long] {
  var id: Long = 0
}

object Genre {

  import SiteDB._

  def allAsString: List[(String, String)] = inTransaction(from(genres)(g => select(g.id.toString, g.name)).toList)

  def get = from(genres)(g => select(g))

  def all: List[Genre] = get.toList
}


case class Rating(trackID: Long, votes: Long, points: Double)


abstract class ObjectTag(objectID: Long, tagID: Long)

case class Tag(name: String) extends DBObject

case class ArtistTag(artistID: Long, tagID: Long) extends ObjectTag(artistID, tagID)

case class AlbumTag(albumID: Long, tagID: Long) extends ObjectTag(albumID, tagID)

case class TrackTag(trackID: Long, tagID: Long) extends ObjectTag(trackID, tagID)

trait TagCreator[T] {


  def insert(item: T, foundTags: List[Tag])

  def find(item: T): List[Tag]


  def delete(item: T)


}

object Tag {

  import SiteDB._

  def byName(s: List[String]) = tags.where(t => t.name in s)

  def search(query: String): List[Tag] = tags.where(t => t.name like "%" + query + "%").toList

  def find(query: List[String]): List[Tag] = byName(query).toList


  implicit val albumTagCreator = new TagCreator[Album] {

    def insert(item: Album, foundTags: List[Tag]) = {
      albumTags.insert(foundTags.map({
        tag => AlbumTag(item.id, tag.id)
      }))
      // create
    }

    def delete(item: Album) = albumTags.deleteWhere(at => at.albumID === item.id)


    def find(item: Album) = join(albumTags, tags)((a, t) =>
      where(a.albumID === item.id)
        select (t)
        on (a.tagID === t.id)
    ).toList
  }
  implicit val trackTagCreator = new TagCreator[Track] {


    def insert(item: Track, foundTags: List[Tag]) = {
      trackTags.insert(foundTags.map({
        tag => TrackTag(item.id, tag.id)
      }))
      // create
    }

    def delete(item: Track) = trackTags.deleteWhere(tt => tt.trackID === item.id)

    def find(item: Track) = {
      join(trackTags, tags)((a, t) =>
        where(a.trackID === item.id)
          select (t)
          on (a.tagID === t.id)
      ).toList
    }
  }

  implicit val artistTagCreator = new TagCreator[Artist] {


    def insert(item: Artist, foundTags: List[Tag]) = {
      artistTags.insert(foundTags.map({
        tag => ArtistTag(item.id, tag.id)
      }))
      // create
    }

    def delete(item: Artist) = artistTags.deleteWhere(at => at.artistID === item.id)

    def find(item: Artist) = {
      join(artistTags, tags)((a, t) =>
        where(a.artistID === item.id)
          select (t)
          on (a.tagID === t.id)
      ).toList
    }

  }

  def forItem[A](item: A)(implicit creator: TagCreator[A]): List[Tag] = creator.find(item)

  def insert[A](item: A, ts: List[String], delete: Boolean = true)(implicit creator: TagCreator[A]): Unit = {

    val filtered = ts.map(_.trim).filter(_.nonEmpty)
    val foundTags = find(filtered)

    // insert tags that are already in db
    creator.delete(item)

    creator.insert(item, foundTags)
    val flattenTags = foundTags.map(_.name)
    // find new tags that were not found in db
    val missingTags = filtered.filter({
      tag => !flattenTags.contains(tag)
    })

    if (!missingTags.isEmpty) {
      // insert the new tags
      tags.insert(missingTags.map(Tag(_)))
      // search for the tags that were newly inserted
      creator.insert(item, Tag.find(missingTags))

    }


  }

}


trait Browser[T] {

}

/*
(filters: T => Seq[LogicalBoolean] = Nil) {

type ItemType = T

def withGenre (genres: Seq[String] ) = copy (filters = (a: T) :: filters)

//def filter(f: LogicalBoolean): Browser[T] =
val table: Table[SaleAbleItem]

def buildWhere () = 1 === 1

def get (page: Int, amount: Int): Query[(ItemType, Artist)] = {
join (table, artists) ((i, a) =>
where (buildWhere () )
select (i, a)
on (i.artistID === a.id)
)
}
}


case class AlbumBrowser(filters: Album => Seq[LogicalBoolean] = Nil) extends Browser[Album](filters) {
  override val table: Table[SaleAbleItem] = albums
}

object Browser {


}

*/
