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


  lazy val find = join(tracks, artists)((t, a) =>
    select(t, a)
      on (t.artistID === a.id)
  )

  def index(page: Int, amount: Int) = TransAction {
    implicit request =>

      val offset = (page - 1) * amount
      val items = find.page(offset, amount).toSeq

      Ok(html.discover(models.Page(items, page - 1, offset, find.Count)))

  }


}
