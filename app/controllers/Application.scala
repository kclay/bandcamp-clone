package controllers

import play.api._
import models._
import actions.Actions._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._
import models.Forms._
import jp.t2v.lab.play20.auth._
import utils.session.SessionHelper
import org.squeryl.PrimitiveTypeMode._
import models.SiteDB._


object Application extends Controller with Auth with MyLoginLogout with AuthConfigImpl with WithDB
{

  def index = optionalUserAction {
    artist => implicit request =>
    // artist.map(_ => {
      Ok(views.html.index())
    /*}).getOrElse({
     val data = Map("username" -> "cideas", "password" -> "cideas")
     db {
       loginForm.bind(data).fold(
         formWithErrors => BadRequest(html.login(formWithErrors)),
         user => {

           models.Artist.updateDomain(user.get.id, "cideas")
           gotoLoginSucceeded(user.get.id)
         }
       )

     }
   }) */


  }


  def fake = Action {
    implicit request =>
      gotoLoginSucceeded(1)
  }

  def validateSignup = Action {
    implicit request =>
      db {
        signupFrom.bindFromRequest.fold(
          errors => BadRequest(html.signup(errors)),
          user => {
            val artist = artists insert Artist(user.username, user.password, user.email, user.name)





            gotoLoginSucceeded(artist.id)
            /* Redirect(routes.Artists.welcome()).withSession(
        session + (SessionHelper.sessionKey -> id.toString))*/
          })
      }

  }

  def signup = Action {
    implicit request =>
      Ok(html.signup(signupFrom))


  }


  def login = Action {
    implicit request =>
      request.method match {
        case "POST" => db {
          loginForm.bindFromRequest.fold(
            formWithErrors => BadRequest(html.login(formWithErrors)),
            user => gotoLoginSucceeded(user.get.id)
          )
        }
        case "GET" => {
          /* val data = Map("username" -> "cideas", "password" -> "cideas")
          loginForm.bind(data).fold(
            formWithErrors => BadRequest(html.login(formWithErrors)),
            user => gotoLoginSucceeded(user.get.id)
          )*/
          Ok(html.login(loginForm))
        }
      }


  }

  def logout = Action {
    implicit request =>
      gotoLogoutSucceeded.flashing(
        "success" -> "You've been logged out"
      )
  }

  def whitelabel = authorizedAction(NormalUser) {
    artist => implicit request =>

      Redirect("http://google.com")
  }


  def changePassword = TODO

  def updatePassword = TODO

  def album(name: String) = WithArtist {
    artist => implicit request =>

      Album.bySlug(artist.id, name).map(album =>
        Ok("hello")
      ) getOrElse (NotFound("Album not found"))

  }

  def page(name: String) = Action {
    Ok("name " + name)

  }

  def track(name: String) = Action {
    Ok("Track")
  }

}