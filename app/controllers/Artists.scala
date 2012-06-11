package controllers


import jp.t2v.lab.play20.auth._
import play.api._
import models._
import actions.Actions._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/5/12
 * Time: 6:59 PM
 */

case class Welcome(genre: Int, tags: Option[String], location: Option[String])

object Artists extends Controller with Auth with AuthConfigImpl
{

  def editAlbum(name: String) = Action {
    Ok("edit album")
  }

  def editTrack(name: String) = Action {
    Ok("edit track")
  }

  def addAlbum = Action {
    Ok("add album")
  }

  def addTrack = Action {
    Ok("add track")
  }

  def list(page: Int, amount: Int, query: String = "") = Action {
    Ok("artists.list")
  }


  val welcomeForm = Form {
    mapping(
      "genre" -> number,
      "tags" -> optional(text),
      "location" -> optional(text)
    )(Welcome.apply)(Welcome.unapply)
  }



  def welcome = Action {
    implicit request =>
      request.method match {
        case "POST" => welcomeForm.bindFromRequest.fold(
          errors => BadRequest(html.artist.welcome(errors, Genres.allAsString)),
          welcome => {
            Ok("hello")
          }
        )
        case "GET" => Ok(html.artist.welcome(welcomeForm, Genres.allAsString))
      }


  }
}
