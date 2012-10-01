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

    }.getOrElse(id, "unknown")


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
        "domain" -> artist.domain,
        "link" -> "http://%s.%s".format(artist.domain, Utils.domain)
      ))
    }
  }
  implicit val trackWriter: Writes[Track] = new Writes[Track] {
    def writes(track: Track) = {
      toObj(Map(

        "kind" -> "track",
        "file" -> track.previewURL(Utils.domain),
        "title" -> track.name,
        "duration" -> String.valueOf(track.duration),
        "link" -> track.url(Utils.domain),
        "slug" -> track.slug,
        "image" -> track.artURL
      )


      )

    }
  }
  implicit val trackWithArtistAlbumWriter: Writes[(Track, Artist, Option[Album], Option[Rating])] = new Writes[(Track, Artist, Option[Album], Option[Rating])] {
    def writes(o: (Track, Artist, Option[Album], Option[Rating])): JsValue = {
      val t = toObj(o._1)

      val a = toObj(Map("artist" -> o._2))
      val ab = toObj(Map("album" -> o._3))
      val r = toObj(Map("rating" -> o._4))
      t ++ a ++ ab ++ r
    }
  }
  implicit val trackWithArtistWriter: Writes[(Track, Artist)] = new Writes[(Track, Artist)] {
    def writes(o: (Track, Artist)): JsValue = {
      val t = toObj(o._1)

      val a = toObj(Map("artist" -> o._2))

      t ++ a
    }
  }

}
