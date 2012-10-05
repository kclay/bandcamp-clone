package json

import play.api.libs.json._
import play.api.cache.Cache
import models._
import play.api.libs.json.Json._
import utils.Utils
import models.Rating
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import models.Rating

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 9/30/12
 * Time: 11:21 PM 
 */
object Writes {

  case class Full[T](obj: T)

  def withGenre(id: Long): String = {
    import play.api.Play.current
    Cache.getOrElse("api_avail_genre") {
      Genre.get.map {
        g => (g.id, g.tag)
      }.toMap

    }.getOrElse(id, "no-genre")


  }

  def toObj[T](o: T)(implicit tjs: Writes[T]): JsObject = tjs.writes(o).asInstanceOf[JsObject]


  implicit val maybeRatingWriter: Writes[Option[Rating]] = new Writes[Option[Rating]] {
    def writes(o: Option[Rating]) = toObj(o.getOrElse(Rating(0, 0, 0)))


  }
  implicit val ratingWriter: Writes[Rating] = new Writes[Rating] {
    def writes(o: Rating) = toJson(Map(
      "votes" -> o.votes.toDouble,
      "points" -> o.points
    ))
  }
  implicit val maybeAlbumWriter: Writes[Option[Album]] = new Writes[Option[Album]] {
    def writes(a: Option[Album]) = a.map(toObj(_)).getOrElse(JsString(""))
  }

  implicit val albumWriter: Writes[Album] = new Writes[Album] {
    def writes(a: Album) = toObj(Map(
      "kind" -> "album",
      "name" -> a.name,
      "about" -> a.about.getOrElse(""),

      "link" -> a.url(Utils.domain),
      "image" -> a.artURL
    ))
  }
  implicit val artistFullWriter: Writes[Full[Artist]] = new Writes[Full[Artist]] {
    def writes(o: Full[Artist]) = toObj(o.obj) ++ toObj(Map("kind" -> "artistFull", "bio" -> o.obj.bio.getOrElse("")))


  }
  implicit val artistWriter: Writes[Artist] = new Writes[Artist] {
    def writes(artist: Artist) = {
      toObj(Map(
        "genre" -> withGenre(artist.genreID),
        "kind" -> "artist",
        "name" -> artist.name,
        "bio" -> artist.bio.getOrElse(""),
        "domain" -> artist.domain,
        "link" -> "http://%s.%s".format(artist.domain, Utils.domain)
      ))
    }
  }
  implicit val trackWriter: Writes[BaseTrack] = new Writes[BaseTrack] {
    def writes(track: BaseTrack) = {
      toObj(Map(

        "kind" -> "track",
        "file" -> track.previewURL(Utils.mediaURL),
        "title" -> track.name,
        "artistName" -> track.artistName.getOrElse(""),
        "duration" -> String.valueOf(track.trackDuration),
        "genre" -> withGenre(track.genreID),
        "slug" -> track.slug,
        "image" -> track.artURL
      )


      )

    }
  }
  implicit val trackWithArtistAlbumWriter: Writes[(BaseTrack, Artist, Option[Album], Option[Rating])] = new Writes[(BaseTrack, Artist, Option[Album], Option[Rating])] {
    def writes(o: (BaseTrack, Artist, Option[Album], Option[Rating])): JsValue = {
      val t = toObj(o._1)

      val a = toObj(Map("artist" -> o._2))
      val ab = toObj(Map("album" -> o._3))
      val r = toObj(Map("rating" -> o._4))
      t ++ a ++ ab ++ r
    }
  }

  implicit def trackWithTacksWithAritstAlbumWriter: Writes[(TrackWithTags, Artist, Option[Album], Option[Rating])] = new Writes[(TrackWithTags, Artist, Option[Album], Option[Rating])] {
    def writes(o: (TrackWithTags, Artist, Option[Album], Option[Rating])) = trackWithArtistAlbumWriter.writes(o)
  }

  implicit val trackWithArtistWriter: Writes[(BaseTrack, Artist)] = new Writes[(BaseTrack, Artist)] {
    def writes(o: (BaseTrack, Artist)): JsValue = {
      val t = toObj(o._1)

      val a = toObj(Map("artist" -> o._2))

      t ++ a
    }
  }

}
