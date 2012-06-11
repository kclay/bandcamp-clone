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

object Actions
{


  def WithArtist(f: Account => Request[AnyContent] => Result) =
  {
    Action {
      request =>
        request.host.split("\\").headOption.flatMap(
          domain => Accounts.findByDomain(domain)
        ).map {

          artist => f(artist)(request)


        }.getOrElse(Results.NotFound("Oops"))
    }
  }
}
