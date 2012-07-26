package utils.format

import play.api.data.format.Formatter
import play.api.data.{Mapping, FormError}
import play.api.data.format.Formats._
import models.Artist
import play.api.data.Forms._
import scala.Left
import play.api.data.FormError
import scala.Some
import scala.Right

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/13/12
 * Time: 12:28 PM
 */

object Formats {

  val artist: Mapping[Long] = of[Long] as artistFormat
  var slug: Mapping[String] = of[String] as slugFormat

  implicit def doubleFormat: Formatter[Double] = new Formatter[Double] {

    override val format = Some("format.real", Nil)

    def bind(key: String, data: Map[String, String]) =
      _parsing(_.toDouble, "error.real", Nil)(key, data)

    def unbind(key: String, value: Double) = Map(key -> value.toString)
  }

  private def _parsing[T](parse: String => T, errMsg: String, errArgs: Seq[Any])(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
    stringFormat.bind(key, data).right.flatMap {
      s =>
        util.control.Exception.allCatch[T]
          .either(parse(s))
          .left.map(e => Seq(FormError(key, errMsg, errArgs)))
    }
  }

  def slugFormat: Formatter[String] = new Formatter[String] {
    def unbind(key: String, value: String) = Map(key -> value)

    def bind(key: String, data: Map[String, String]) = {
      import utils.Utils.slugify

      val nameKey = key.split("\\.").head + ".name"
      Right(data.get(nameKey)).right.flatMap {
        case Some(name) => Right(slugify(name))

        case _ => Left(Seq(FormError(key, "error.slug", Nil)))
      }
    }
  }

  def artistFormat: Formatter[Long] = new Formatter[Long] {
    override val format = Some("format.numeric", Nil)

    def unbind(key: String, value: Long) = Map(key -> value.toString)

    def bind(key: String, data: Map[String, String]) = {
      import utils.Utils.artistId
      artistId.map {
        Right(_)
      }.getOrElse(Left(Seq(FormError(key, "error.artist", Nil))))

    }
  }
}
