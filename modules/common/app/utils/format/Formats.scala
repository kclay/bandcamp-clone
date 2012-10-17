package utils.format

import play.api.data.format.Formatter
import play.api.data.{ObjectMapping18, ObjectMapping, Mapping, FormError}
import play.api.data.format.Formats._
import models.Artist
import play.api.data.Forms._
import scala.Left
import scala.Some
import scala.Right
import play.api.data.validation.Constraint
import scala.Left
import play.api.data.FormError
import scala.Some
import scala.Right
import utils.Utils


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

      val nameKey = if (key.contains(".")) key.split("\\.").head + ".name" else key
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

  implicit def artistEmailFormat: Formatter[Artist] = new Formatter[Artist] {


    def unbind(key: String, value: Artist) = {

      Map(key -> value.email)
    }


    def bind(key: String, data: Map[String, String]) = {

      lazy val error = Left(Seq(FormError(key, "error.artistEmail", Nil)))
      Right(data.get(key)).right.flatMap {
        case Some(emailOrName) =>
          Artist.byEmailOrName(emailOrName)
            .map(Right(_))
            .getOrElse(error)

        case _ => error
      }


    }
  }

  val artistEmail: Mapping[Artist] = of[Artist]

  /*
def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18,A19](a1: (String, Mapping[A1]), a2: (String, Mapping[A2]), a3: (String, Mapping[A3]), a4: (String, Mapping[A4]), a5: (String, Mapping[A5]), a6: (String, Mapping[A6]), a7: (String, Mapping[A7]), a8: (String, Mapping[A8]), a9: (String, Mapping[A9]), a10: (String, Mapping[A10]), a11: (String, Mapping[A11]), a12: (String, Mapping[A12]), a13: (String, Mapping[A13]), a14: (String, Mapping[A14]), a15: (String, Mapping[A15]), a16: (String, Mapping[A16]), a17: (String, Mapping[A17]), a18: (String, Mapping[A18]),a19: (String, Mapping[A19]))(apply: Function19[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18,A19, R])(unapply: Function1[R, Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18,A19)]]): Mapping[R] = {
  ObjectMapping19(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18,a19)
}

case class ObjectMapping19[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19](apply: Function19[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, R], unapply: Function1[R, Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)]], f1: (String, Mapping[A1]), f2: (String, Mapping[A2]), f3: (String, Mapping[A3]), f4: (String, Mapping[A4]), f5: (String, Mapping[A5]), f6: (String, Mapping[A6]), f7: (String, Mapping[A7]), f8: (String, Mapping[A8]), f9: (String, Mapping[A9]), f10: (String, Mapping[A10]), f11: (String, Mapping[A11]), f12: (String, Mapping[A12]), f13: (String, Mapping[A13]), f14: (String, Mapping[A14]), f15: (String, Mapping[A15]), f16: (String, Mapping[A16]), f17: (String, Mapping[A17]), f18: (String, Mapping[A18]), f19: (String, Mapping[A19]), val key: String = "", val constraints: Seq[Constraint[R]] = Nil) extends Mapping[R] with ObjectMapping {

  val field1 = f1._2.withPrefix(f1._1).withPrefix(key)

  val field2 = f2._2.withPrefix(f2._1).withPrefix(key)

  val field3 = f3._2.withPrefix(f3._1).withPrefix(key)

  val field4 = f4._2.withPrefix(f4._1).withPrefix(key)

  val field5 = f5._2.withPrefix(f5._1).withPrefix(key)

  val field6 = f6._2.withPrefix(f6._1).withPrefix(key)

  val field7 = f7._2.withPrefix(f7._1).withPrefix(key)

  val field8 = f8._2.withPrefix(f8._1).withPrefix(key)

  val field9 = f9._2.withPrefix(f9._1).withPrefix(key)

  val field10 = f10._2.withPrefix(f10._1).withPrefix(key)

  val field11 = f11._2.withPrefix(f11._1).withPrefix(key)

  val field12 = f12._2.withPrefix(f12._1).withPrefix(key)

  val field13 = f13._2.withPrefix(f13._1).withPrefix(key)

  val field14 = f14._2.withPrefix(f14._1).withPrefix(key)

  val field15 = f15._2.withPrefix(f15._1).withPrefix(key)

  val field16 = f16._2.withPrefix(f16._1).withPrefix(key)

  val field17 = f17._2.withPrefix(f17._1).withPrefix(key)

  val field18 = f18._2.withPrefix(f18._1).withPrefix(key)
  val field19 = f19._2.withPrefix(f19._1).withPrefix(key)

  def bind(data: Map[String, String]): Either[Seq[FormError], R] = {
    merge(field1.bind(data), field2.bind(data), field3.bind(data), field4.bind(data), field5.bind(data), field6.bind(data), field7.bind(data), field8.bind(data), field9.bind(data), field10.bind(data), field11.bind(data), field12.bind(data), field13.bind(data), field14.bind(data), field15.bind(data), field16.bind(data), field17.bind(data), field18.bind(data)) match {
      case Left(errors) => Left(errors)
      case Right(values) => {
        applyConstraints(apply(

          values(0).asInstanceOf[A1],
          values(1).asInstanceOf[A2],
          values(2).asInstanceOf[A3],
          values(3).asInstanceOf[A4],
          values(4).asInstanceOf[A5],
          values(5).asInstanceOf[A6],
          values(6).asInstanceOf[A7],
          values(7).asInstanceOf[A8],
          values(8).asInstanceOf[A9],
          values(9).asInstanceOf[A10],
          values(10).asInstanceOf[A11],
          values(11).asInstanceOf[A12],
          values(12).asInstanceOf[A13],
          values(13).asInstanceOf[A14],
          values(14).asInstanceOf[A15],
          values(15).asInstanceOf[A16],
          values(16).asInstanceOf[A17],
          values(17).asInstanceOf[A18],
          values(18).asInstanceOf[A19]))
      }
    }
  }

  def unbind(value: R): (Map[String, String], Seq[FormError]) = {
    unapply(value).map {
      fields =>
        val (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19) = fields
        val a1 = field1.unbind(v1)
        val a2 = field2.unbind(v2)
        val a3 = field3.unbind(v3)
        val a4 = field4.unbind(v4)
        val a5 = field5.unbind(v5)
        val a6 = field6.unbind(v6)
        val a7 = field7.unbind(v7)
        val a8 = field8.unbind(v8)
        val a9 = field9.unbind(v9)
        val a10 = field10.unbind(v10)
        val a11 = field11.unbind(v11)
        val a12 = field12.unbind(v12)
        val a13 = field13.unbind(v13)
        val a14 = field14.unbind(v14)
        val a15 = field15.unbind(v15)
        val a16 = field16.unbind(v16)
        val a17 = field17.unbind(v17)
        val a18 = field18.unbind(v18)
        val a19 = field19.unbind(v19)

        (a1._1 ++ a2._1 ++ a3._1 ++ a4._1 ++ a5._1 ++ a6._1 ++ a7._1 ++ a8._1 ++ a9._1 ++ a10._1 ++ a11._1 ++ a12._1 ++ a13._1 ++ a14._1 ++ a15._1 ++ a16._1 ++ a17._1 ++ a18._1 ++ a19._1) ->
          (a1._2 ++ a2._2 ++ a3._2 ++ a4._2 ++ a5._2 ++ a6._2 ++ a7._2 ++ a8._2 ++ a9._2 ++ a10._2 ++ a11._2 ++ a12._2 ++ a13._2 ++ a14._2 ++ a15._2 ++ a16._2 ++ a17._2 ++ a18._2 ++ a19._2)
    }.getOrElse(Map.empty -> Seq(FormError(key, "unbind.failed")))
  }

  def withPrefix(prefix: String): ObjectMapping19[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19] = addPrefix(prefix).map(newKey => this.copy(key = newKey)).getOrElse(this)

  def verifying(addConstraints: Constraint[R]*): ObjectMapping19[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19] = {
    this.copy(constraints = constraints ++ addConstraints.toSeq)
  }

  val mappings = Seq(this) ++ field1.mappings ++ field2.mappings ++ field3.mappings ++ field4.mappings ++ field5.mappings ++ field6.mappings ++ field7.mappings ++ field8.mappings ++ field9.mappings ++ field10.mappings ++ field11.mappings ++ field12.mappings ++ field13.mappings ++ field14.mappings ++ field15.mappings ++ field16.mappings ++ field17.mappings ++ field18.mappings ++ field19.mappings

}  */

}
