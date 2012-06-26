package data.format

import play.api.data._
import play.api.data.format._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/13/12
 * Time: 11:45 AM
 */

object Formats
{
  implicit def doubleFormat: Formatter[Double] = new Formatter[Double]
  {

    override val format = Some("format.real", Nil)

    def bind(key: String, data: Map[String, String]) =
      parsing(_.toDouble, "error.real", Nil)(key, data)

    def unbind(key: String, value: Double) = Map(key -> value.toString)
  }

   def myStringFormat: Formatter[String] = new Formatter[String] {
    def bind(key: String, data: Map[String, String]) = data.get(key).toRight(Seq(FormError(key, "error.required", Nil)))
    def unbind(key: String, value: String) = Map(key -> value)
  }
  implicit def unknownLongFormat: Formatter[Long] = new Formatter[Long]
  {

    override val format = Some("format.numeric", Nil)

    def bind(key: String, data: Map[String, String]) =
    {
      myStringFormat.bind(key, data).right.flatMap {
        s =>
          scala.util.control.Exception.allCatch[Long]
            .either(0)
            .left.map(e => Seq(FormError(key, "error.number", Nil)))
      }
    }

    def unbind(key: String, value: Long) = Map.empty
  }
}
