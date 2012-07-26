package binders

import play.api.mvc.QueryStringBindable

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 7/25/12
 * Time: 11:20 PM
 */

object Binders {

  import java.lang.{Double => JDouble}

  implicit val doubleBinder: QueryStringBindable[Double] = new QueryStringBindable[Double] {
    def bind(key: String, params: Map[String, Seq[String]]) = params.get(key).flatMap(_.headOption).map {
      i =>
        try {
          Right(java.lang.Double.parseDouble(i))
        } catch {
          case e: NumberFormatException => Left("Cannot parse parameter " + key + " as Double: " + e.getMessage)
        }
    }

    def unbind(key: String, value: Double) = key + "=" + value.toString
  }
}
