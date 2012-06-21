package controllers

import play.api._
import play.api.mvc._
import models._
import models.Forms._
import com.codahale.jerkson.Json._
import jp.t2v.lab.play20.auth.Auth
import views.html


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/11/12
 * Time: 11:18 AM
 */

object Ajax extends Controller with Auth with AuthConfigImpl
{

  def tags(query: String) = Action {
    val found = Tags.search(query).map {
      case (t) => Map("name" -> t.name)
    }


    Ok(generate(found))
  }

  def insertTrack() = authorizedAction(NormalUser) {
    implicit artist => implicit request =>
      singleTrackForm.bindFromRequest.fold(
        errors => BadRequest(""),
        value => {

          Ok(generate(Tracks.create(value)))
        }
      )
  }




}
