package controllers

import play.api.mvc.Controller
import jp.t2v.lab.play20.auth.Auth
import models._
import actions.{Authorizer, SquerylTransaction}
import actions.Actions._

import com.codahale.jerkson.Json._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 8/19/12
 * Time: 4:17 PM
 */
object Stats extends Controller with Auth with AuthConfigImpl with WithDB with SquerylTransaction with Authorizer {

  def plays(metric: Metric, range: Range) = TransAction {
    Authorize {
      implicit artist => implicit request =>
        Ok(g(metric.asInstanceOf[TrackMetric] ~ range))
    }
  }

  private def g(obj: Any) = generate(obj)

  def sales(metric: Metric, range: Range) = TransAction {
    Authorize {
      implicit artist => implicit request =>
        Ok(g(metric.asInstanceOf[PurchaseMetric] ~ range))
    }
  }


  def track(metric: Metric, objectID: Long, remove: Boolean) = TransAction {
    WithArtist {
      artist => implicit request =>
        if (remove) {
          Stat.remove(metric, artist.id, objectID)
        } else {
          Stat(metric, artist.id, objectID)
        }
        Ok
    }
  }
}
