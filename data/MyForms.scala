package data

import play.api.data.Mapping



/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/13/12
 * Time: 11:54 AM
 */

object MyForms
{
  def unknownLong[A](value: A): Mapping[A] = of(unknownLong(value))

  def double[A](value: A): Mapping[A] = of(doubleFormat(value))
}
