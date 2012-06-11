package controllers

import play.api.mvc._
import models._


;


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/4/12
 * Time: 1:07 PM
 */

object Albums extends Controller
{
 /* def WithArtist(f: Artist => Request[AnyContent] => Result) =
  {
    Action {
      request =>
        request.host.split("\\").head.map(
          domain => Artist.findByDomain(domain))
          .map {
          artist => f(artist, request)

        }.getOrElse(Unauthorized)
    }
  }    */

 /* def index(name: String) = WithArtist {
    artist => implicit request =>
      Album.findBySlug(artist.id, name)

  }*/
  def index(name:String)=Action{
   Ok("Yo")
 }

  /*
  def track(name: String) = WithArtist {
    artist => implicit request =>
    // Track.findBySlug(artist.id, name)
  }*/

}
