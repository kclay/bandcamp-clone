package utils

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 7/25/12
 * Time: 12:48 AM
 */


object Utils {


  def slugify(str: String): String = {
    import java.text.Normalizer
    Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\w ]", "").replace(" ", "-").toLowerCase
  }

  private val CONTEXT_USER_ID = "_user_id_"


  def artistId(id: Long) = {
    import play.mvc.Http

    Context.current().args.put(CONTEXT_USER_ID, id.asInstanceOf[Object])
  }

  def urldecode(data: String) = java.net.URLDecoder.decode(data, "UTF-8").split("\u0000").map(_.split(":")).map(p => p(0) -> p.drop(1).mkString(":")).toMap

  def artistId = {
    import play.mvc.Http
    import binders._

    import java.lang.{Long => JLong}
    val args = Context.current().args
    if (args.containsKey(CONTEXT_USER_ID)) Some(args.get(CONTEXT_USER_ID).asInstanceOf[Long]) else None

  }
}