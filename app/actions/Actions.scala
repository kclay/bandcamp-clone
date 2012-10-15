package actions

import controllers.routes
import jp.t2v.lab.play20.auth.{AuthConfig, Auth}
import models.{NormalUser, Artist}
import org.squeryl.PrimitiveTypeMode._
import play.api.mvc._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 10/15/12
 * Time: 11:41 AM 
 */


trait Authorizer {
  self: Controller with Auth with AuthConfig =>
  def Authorize(f: Artist => Request[AnyContent] => Result) = Action {
    implicit request =>

      val parts = request.host.split("\\.")
      val subdomain = parts.head
      val domain = parts.drop(1).mkString(".")


      inTransaction {
        authorized(NormalUser.asInstanceOf[this.Authority])(request).right.map(
          u => if (u.asInstanceOf[Artist].domain.equals(subdomain)) f(u.asInstanceOf[Artist])(request) else Redirect("http://" + domain + routes.Artists.index().url)
        ).merge
      }

  }
}


