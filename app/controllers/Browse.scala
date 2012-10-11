package controllers

import play.api.mvc.Controller
import actions.SquerylTransaction
import org.squeryl.{Table, PrimitiveTypeMode}
import models.{Artist, Album, SaleAbleItem}
import views._
import org.squeryl.dsl.ast.BinaryOperatorNodeLogicalBoolean

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 10/2/12
 * Time: 8:34 AM 
 */

import models.SiteDB._
import PrimitiveTypeMode._

object Browse extends Controller with SquerylTransaction {


  /* lazy val find = join(tracks, artists)((t, a) =>
 select(t, a)
   on (t.artistID === a.id)
)   */
  lazy val find = join(tracks, artists, albumTracks.leftOuter, albums.leftOuter)((t, a, at, ab) =>
    where(t.active === true)
      select(t, a, ab)
      orderBy (t.id.desc)
      on(t.artistID === a.id,
      t.id === at.map(_.trackID),
      at.map(_.albumID) === ab.map(_.id))
  )

  def index(page: Int, amount: Int) = TransAction {
    implicit request =>

      val offset = (page - 1) * amount
      val items = find.page(offset, amount).toSeq
      val count = tracks.where(t => t.active === true).Count
      Ok(html.discover(models.Page(items, page - 1, offset, count)))

  }


}
