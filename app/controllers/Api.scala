package controllers

import actions.SquerylTransaction

import play.api.mvc._
import models.SiteDB._
import org.squeryl.PrimitiveTypeMode._
import models.{Track, Artist, Genre, Album}
import utils.Utils


import play.api.libs.json._
import play.api.libs.json.Json._
import com.codahale.jerkson.Json._
import play.api.cache.Cache

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 9/17/12
 * Time: 12:21 PM 
 */
object Api extends Controller with SquerylTransaction {


  def withGenre(id: Long): String = {
    import play.api.Play.current
    Cache.getOrElse("api_avail_genre") {
      Genre.get.map {
        g => (g.id, g.tag)
      }.toMap

    }.getOrElse(id, "unknown")


  }

  implicit val maybeOlbumWriter: Writes[Option[Album]] = new Writes[Option[Album]] {
    def writes(a: Option[Album]) = a.map(toJson(_)).getOrElse(JsString(""))
  }
  implicit val albumWriter: Writes[Album] = new Writes[Album] {
    def writes(a: Album) = toJson(Map(
      "kind" -> "album",
      "name" -> a.name,

      "link" -> a.url(Utils.domain),
      "image" -> a.artURL
    ))
  }
  implicit val artistWriter: Writes[Artist] = new Writes[Artist] {
    def writes(artist: Artist): JsValue = {
      toJson(Map(
        "genre" -> withGenre(artist.genreID),
        "kind" -> "artist",
        "name" -> artist.name,
        "link" -> "http://%s.%s".format(artist.domain, Utils.domain)
      ))
    }
  }
  implicit val trackWriter: Writes[Track] = new Writes[Track] {
    def writes(track: Track): JsValue = {
      toJson(Map(

        "kind" -> "track",
        "file" -> track.previewURL(Utils.domain),
        "title" -> track.name,
        "duration" -> String.valueOf(track.duration),
        "link" -> track.url(Utils.domain),
        "image" -> track.artURL
      )


      )

    }
  }
  implicit val trackWithArtistAlbumWriter: Writes[(Track, Artist, Option[Album])] = new Writes[(Track, Artist, Option[Album])] {
    def writes(o: (Track, Artist, Option[Album])): JsValue = {
      val t = toJson(o._1).asInstanceOf[JsObject]

      val a = toJson(Map("artist" -> o._2)).asInstanceOf[JsObject]
      val ab = toJson(Map("album" -> o._3)).asInstanceOf[JsObject]
      t ++ a ++ ab
    }
  }
  implicit val trackWithArtistWriter: Writes[(Track, Artist)] = new Writes[(Track, Artist)] {
    def writes(o: (Track, Artist)): JsValue = {
      val t = toJson(o._1).asInstanceOf[JsObject]

      val a = toJson(Map("artist" -> o._2)).asInstanceOf[JsObject]

      t ++ a
    }
  }

  def writeResults(kind: String, results: JsArray) = JsObject(Seq(("kind", JsString(kind)), ("results", results))).toString


  def withTags(tags: String) = join(tracks, albumTracks.leftOuter, albums.leftOuter, artists)((t, at, ab, a) =>
    where(a.genreID in from(genres)(
      g => where(g.tag in tags.split(",").toSeq)
        select (g.id)
    ))
      select(t, a, ab)
      on(
      at.map(_.trackID) === t.id,
      at.map(_.albumID) === ab.map(_.id),
      t.artistID === a.id
      )

  )

  def prepQuery(query: String) = "%" + query + "%"

  def withQuery(query: String, tags: String) = join(tracks, albumTracks.leftOuter, albums.leftOuter, artists)((t, at, ab, a) =>
    where(
      a.genreID in from(genres)(
        g => where(g.tag in tags.split(",").toSeq)
          select (g.id)
      ) and (t.name like prepQuery(query)
        or (a.name like prepQuery(query))
        )
    )


      select(t, a, ab)
      on(
      at.map(_.trackID) === t.id,
      at.map(_.albumID) === ab.map(_.id),
      t.artistID === a.id
      )

  )

  def fetch(tags: String, query: Option[String], page: Int, amount: Int) = TransAction {

    Action {


      val rs = query.map(withQuery(_, tags))
        .getOrElse(withTags(tags))
        .page((page - 1) * amount, amount).map(toJson(_)).toSeq


      Ok(writeResults("tracks", JsArray(rs)))
    }

  }
}
