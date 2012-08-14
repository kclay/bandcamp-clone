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
import mvc._
import play.api.http.Status._
import org.squeryl.PrimitiveTypeMode.inTransaction
import jp.t2v.lab.play20.auth.{AuthConfig, Auth}
import controllers.routes


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

trait Authorizer {
  self: Controller with Auth with AuthConfig =>
  def Authorize(f: Artist => Request[AnyContent] => Result) = Action {
    implicit request =>

      val parts = request.host.split("\\.")
      val subdomain = parts.head
      val domain = parts.drop(1).mkString(".")



      authorized(NormalUser.asInstanceOf[this.Authority])(request).right.map(
        u => if (u.asInstanceOf[Artist].domain.equals(subdomain)) f(u.asInstanceOf[Artist])(request) else Redirect("http://" + domain + routes.Artists.index().url)
      ).merge

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
