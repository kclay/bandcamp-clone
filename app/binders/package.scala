package binders

import play.api.mvc.PathBindable
import java.lang.{Double => JDouble}
import play.api.mvc.QueryStringBindable
import models._
import scala.Left
import scala.Right

object `package` {

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

  implicit def metricBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[Metric] {
    def unbind(key: String, value: Metric) = stringBinder.unbind(key, value.name)


    def withMetric(value: String) = {

      val metric = value match {

        case Partial(_) => Partial
        case Play(_) => Play

        case Skip(_) => Skip
        case Complete(_) => Complete
        case Sales(_) => Sales
        case _ => InvalidMetric

      }
      if (metric.equals(InvalidMetric)) None else Some(metric)


    }

    def bind(key: String, value: String): Either[String, Metric] = for {
      m <- stringBinder.bind(key, value).right
      metric <- withMetric(m).toRight("Metric not found").right

    } yield metric
  }

  implicit def rangeBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[Range] {
    def unbind(key: String, value: Range) = stringBinder.unbind(key, value.name)


    def withRange(value: String) = {
      val range = value match {

        case AllTime(_) => AllTime
        case Today(_) => Today

        case Month(_) => Month
        case TwoMonths(_) => TwoMonths
        case Week(_) => Week
        case _ => InvalidRange

      }
      if (range.equals(InvalidRange)) None else Some(range)


    }

    def bind(key: String, value: String): Either[String, Range] = for {
      m <- stringBinder.bind(key, value).right
      range <- withRange(m).toRight("Invalid Range").right

    } yield range
  }
}