package actions
import models.Artist
import org.squeryl.PrimitiveTypeMode._
import play.api.mvc._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 10/15/12
 * Time: 11:55 AM 
 */
trait SquerylTransaction {
  def TransAction(f: Request[AnyContent] => Result) = Action {

    implicit request =>

      inTransaction {
        f(request)
      }
  }

}

trait WithCommon extends SquerylTransaction {
  def WithArtist(f: Artist => Request[AnyContent] => Result) = {
    Action {
      implicit request =>
        request.host.split("\\.").headOption.map {
          domain =>
            inTransaction {
              models.Artist.findByDomain(domain).map(f(_)(request))
                .getOrElse(Results.Redirect("/signup?new_domain=%s".format(domain)))
            }

        }.getOrElse(Results.NotFound)

    }
  }
}
