package utils.format

import play.api.data.format.Formatter
import play.api.data.FormError
import play.api.data.format.Formats._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/13/12
 * Time: 12:28 PM
 */

object Formats
{
  implicit def doubleFormat: Formatter[Double] = new Formatter[Double]
  {

    override val format = Some("format.real", Nil)

    def bind(key: String, data: Map[String, String]) =
     _parsing(_.toDouble, "error.real", Nil)(key, data)

    def unbind(key: String, value: Double) = Map(key -> value.toString)
  }

  private def _parsing[T](parse: String => T, errMsg: String, errArgs: Seq[Any])(key: String, data: Map[String, String]): Either[Seq[FormError], T] =
  {
    stringFormat.bind(key, data).right.flatMap {
      s =>
        util.control.Exception.allCatch[T]
          .either(parse(s))
          .left.map(e => Seq(FormError(key, errMsg, errArgs)))
    }
  }
}
