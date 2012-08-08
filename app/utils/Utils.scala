package utils

import models.{SaleAbleItem, Artist}
import play.api.mvc.{Results, RequestHeader}
import play.api.libs.Crypto
import play.api.Logger

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
    import play.mvc.Http

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
    import play.mvc.Http
    import binders._

    import java.lang.{Long => JLong}
    val args = Context.current().args

    if (args.containsKey(CONTEXT_USER_ID)) Some(args.get(CONTEXT_USER_ID).asInstanceOf[Long]) else None

  }

  def devMode = {
    import play.api.Play.current
    play.api.Play.isDev
  }

  def mediaURL = {
    import play.api.Play.current
    import play.api.Play

    Play.configuration.getString("server.media").get
  }

  def uploadURL = {
    import play.api.Play.current
    import play.api.Play
    Play.configuration.getString("server.upload").get
  }

  def withArtist(request: RequestHeader) = {
    for {
      domain <- request.host.split("\\.").headOption
      artist <- models.Artist.findByDomain(domain)
    } yield artist

  }

  def mod(a: Int, b: Int) = a % b == 0
}
