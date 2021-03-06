package utils

import models.Artist
import play.api.mvc.RequestHeader
import play.api.cache.Cache


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 7/25/12
 * Time: 12:48 AM
 */


object Utils {


  def normalize(str: String, whiteSpace: String = "-"): String = {
    import java.text.Normalizer
    Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\w ]", "").replace(" ", whiteSpace)
  }

  def price(p: Double) = {
    import java.text.NumberFormat
    NumberFormat.getCurrencyInstance().format(p)
  }

  def slugify(str: String, lower: Boolean = true): String = {

    normalize(str, "-").toLowerCase
  }

  private val CONTEXT_USER_ID = "_user_id_"
  private val CONTEXT_ARTIST = "_artist_"


  def artistId(id: Long) = {

    Context.current().args.put(CONTEXT_USER_ID, id.asInstanceOf[Object])
  }

  def urldecode(data: String) = java.net.URLDecoder.decode(data, "UTF-8").split("\u0000").map(_.split(":")).map(p => p(0) -> p.drop(1).mkString(":")).toMap

  def artist = {
    val args = Context.current().args
    if (args.containsKey(CONTEXT_ARTIST)) Some(args.get(CONTEXT_ARTIST).asInstanceOf[Artist]) else None
  }

  def artist(artist: Option[Artist]) = {
    artist.map {
      a =>
        artistId(a.id)
        Context.current().args.put(CONTEXT_ARTIST, a)
    }

  }

  def artistId = {

    import java.lang.{Long => JLong}
    val args = Context.current().args

    if (args.containsKey(CONTEXT_USER_ID)) Some(args.get(CONTEXT_USER_ID).asInstanceOf[Long]) else None

  }

  def devMode = {
    import play.api.Play.current
    play.api.Play.isDev
  }

  def serverURL(name: String) = {
    import play.api.Play.current
    import play.api.Play
    Play.configuration.getString("server." + name).get

  }

  def mediaURL = serverURL("media")

  def domain = serverURL("domain")

  def uploadURL = serverURL("upload")

  def withArtist(request: RequestHeader) = {
    for {
      domain <- request.host.split("\\.").headOption
      artist <- models.Artist.findByDomain(domain)
    } yield artist

  }

  def genreByID(id: Long) = {
    import play.api.Play.current
    Cache.getOrElse("genre_by_id") {
      import models.Genre
      Genre.get.map {
        g => (g.id, g)
      }.toMap

    }.get(id)
  }

  def mod(a: Int, b: Int) = a % b == 0
}
