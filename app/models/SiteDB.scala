package models

import utils.Image
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.{Query, Session, KeyedEntity, Schema}
import org.squeryl.annotations.Column


import java.sql.Date

import play.api.Logger
import org.squeryl.dsl.CompositeKey2

import scala.Some


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/20/12
 * Time: 7:30 PM
 */

class DBObject extends KeyedEntity[Long]
{
  var id: Long = 0
}

case class Tag(name: String) extends DBObject

object Tag
{

  import SiteDB._

  def byName(s: List[String]) = tags.where(t => t.name in s)

  def search(query: String): List[Tag] = tags.where(t => t.name like "%" + query + "%").toList

  def find(query: List[String]): List[Tag] = byName(query).toList
}

case class Genre(name: String) extends DBObject

object Genre
{

  import SiteDB._

  def allAsString: List[(String, String)] = inTransaction(from(genres)(g => select(g.id.toString, g.name)).toList)

  def all: List[Genre] = from(genres)(g => select(g)).toList
}


case class ArtistTag(artistID: Long, tagID: Long)

object ArtistTag
{


  import SiteDB._

  def insert(artist: Artist, ts: List[String]) = inTransaction {


    val foundTags = Tag.find(ts)
    // insert tags that are already in db
    artistTags.insert(foundTags.map({
      tag => ArtistTag(artist.id, tag.id)
    }))
    // create flatten List[String]
    val flattenTags = foundTags.map(_.name)
    // find new tags that were not found in db
    val missingTags = ts.filter({
      tag => !flattenTags.contains(tag)
    })

    if (!missingTags.isEmpty) {
      // insert the new tags
      tags.insert(missingTags.map(Tag(_)))
      // search for the tags that were newly inserted
      artistTags.insert(Tag.find(missingTags).map({
        tag => ArtistTag(artist.id, tag.id)
      }))
    }


  }
}

object Artist
{
  private val SALT: String = "m^c*$kxz_qkwupq$by*fpi_czho=#8+k5dnakvd7x$gt#-&h+t";

  import SiteDB._

  def hasTag(id: Long): Boolean = inTransaction(artistTags.where(t => t.artistID === id).Count != 0)

  def domainAvail(domain: String): Boolean = artists.where(a => a.domain === domain).Count == 1


  def findByDomain(domain: String): Option[Artist] = inTransaction(artists.where(a => a.domain === domain).headOption)

  def authenticate(name: String, password: String): Option[Artist] = findByUsername(name).filter(a => a.pass == hash(password))

  def findByUsername(username: String): Option[Artist] = artists.where(a => a.username === username).headOption

  private def hash(pass: String): String = pass

  def findByEmail(email: String): Option[Artist] = artists.where(a => a.email === email).headOption

  def find(id: Long): Option[Artist] = artists.where(a => a.id === id).headOption

  def updateDomain(artistId: Long, domain: String) = inTransaction {
    update(artists)(a =>
      where(a.id === artistId)
        set (a.domain := domain,a.activated:=true)
    )
  }


}

case class Artist(username: String, pass: String, email: String, name: String, domain: String = "", permission: String = "normal", activated: Boolean = false) extends DBObject


case class Album(var artistID: Long, name: String, artistName: Option[String], slug: String, active: Boolean = false,
                 download: Boolean = true, donateMore: Boolean = true, price: Double = 1.00,
                 art: Option[String], about: Option[String], credits: Option[String], upc: Option[String], releaseDate: Option[Date]) extends DBObject
{
  def this() = this(0, "", Some(""), "", false, true, true, 1.00, Some(""), Some(""), Some(""), Some(""), Some(new Date(System.currentTimeMillis)))

  lazy val artURL: String = art.map(Image(_).url).getOrElse("")


}

object Album
{

  import SiteDB._

  def bySlug(artistId: Long, slug: String) =
    from(albums)(a =>
      where(a.artistID === artistId and a.slug === slug)
        select (a)
    ).headOption

  def validateSlug(artistId: Long, slug: String) =
    bySlug(artistId, slug).map(a => true).getOrElse(false)


  def withTracks(albumID: Long): List[Option[Track]] =
    join(albumTracks, tracks.leftOuter)((at, t) =>
      where(at.albumID === albumID)
        select (t)
        on (at.trackID === t.map(_.id))

    ).iterator.toList
}

case class Track(var id: Long = 0, var artistID: Long, name: String, donateMore: Boolean = true, download: Boolean = true, price: Double = 1.00,
                 license: String, artistName: Option[String],
                 art: Option[String], lyrics: Option[String], about: Option[String], credits: Option[String], releaseDate: Option[Date], active: Boolean = false) extends KeyedEntity[Long]
{
  def this() = this(0, 0, "", true, true, 1.00, "", Some(""), Some(""), Some(""), Some(""), Some(""), Some(new Date(System.currentTimeMillis)), false)


}

object Track
{

  import SiteDB._

  def find(id: Long): Option[Track] = tracks.where(t => t.id === id).headOption

  def publish(id: Long) = update(tracks)(t =>
    where(t.id === id)
      set (t.active := true)
  )
}


case class AlbumTracks(@Column("album_id") albumID: Long, @Column("track_id") trackID: Long, order: Int)
{


}


object SiteDB extends Schema
{

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
    t.about is (dbType("text")),
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


}



