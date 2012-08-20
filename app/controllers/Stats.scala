package controllers

import play.api.mvc.Controller
import jp.t2v.lab.play20.auth.Auth
import models._
import actions.{Authorizer, SquerylTransaction}
import actions.Actions._

import com.codahale.jerkson.Json._
import utils.Utils.withArtist

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 8/19/12
 * Time: 4:17 PM
 */
object Stats extends Controller with Auth with AuthConfigImpl with WithDB with SquerylTransaction with Authorizer {

  def plays(range: Range) = TransAction {
    Authorize {
      implicit artist => implicit request =>
        Ok(g(Play ~ range))
    }
  }

  private def g(obj: Any) = generate(obj)

  def sales(range: Range) = TransAction {
    Authorize {
      implicit artist => implicit request =>
        val tracks = range ~ PurchaseTrack
        val albums = range ~ PurchaseAlbum
        //val all = (tracks.toList ++ albums.toList).groupBy(_._1).mapValues(_.map(_._2.flatten))

        Ok(g(Map("tracks" -> tracks, "albums" -> albums)))
    }
  }


  def track(metric: Metric, objectID: Long, remove: Boolean) = TransAction {
    optionalUserAction {
      user => implicit request =>

        withArtist(request).map {
          artist => {
            if (user.isEmpty || user.get.id != artist.id) {
              if (remove) {
                Stat.remove(metric, artist.id, objectID)
              } else {
                Stat(metric, artist.id, objectID)
              }
            }
            Ok
          }
        }.getOrElse(Ok)
    }


  }

}
