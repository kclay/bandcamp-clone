package actions

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/8/12
 * Time: 7:57 PM
 */

import models._
import play.api._

import play.api.http._
import play.api.mvc._
import play.api.http.Status._
import org.squeryl.PrimitiveTypeMode.inTransaction


trait SquerylTransaction {
  def TransAction(f: Request[AnyContent] => Result): Action[AnyContent] = {
    Action {
      implicit request =>
        inTransaction {
          f(request)
        }
    }
  }
}

object Actions {


  def WithArtist(f: Artist => Request[AnyContent] => Result) = {
    Action {
      implicit request =>
        request.host.split("\\.").headOption.map {
          domain =>
            models.Artist.findByDomain(domain).map(f(_)(request))
              .getOrElse(Results.Redirect("/signup?new_domain=%s".format(domain)))

        }.getOrElse(Results.NotFound)

    }
  }


}
