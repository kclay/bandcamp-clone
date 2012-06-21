package controllers

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/21/12
 * Time: 11:32 AM
 */


trait WithDB
{
  def db[A](a: => A): A =
  {
    import org.squeryl.PrimitiveTypeMode._

    inTransaction(a)
  }
}
