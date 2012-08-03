package controllers

import models._
import actions.Actions._
import play.api._
import libs.concurrent.Akka
import play.api.mvc._
import views._
import models.Forms._
import jp.t2v.lab.play20.auth._
import models.SiteDB._
import actions.SquerylTransaction


import utils.Zip
import java.io.File
import java.util.concurrent.Callable


object Application extends Controller with Auth with MyLoginLogout with AuthConfigImpl with WithDB with SquerylTransaction {

  def javascriptRoutes = Action {
    implicit request =>
      import routes.javascript._
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          Upload.audio, Upload.art, Upload.audioUploaded, Upload.status,
          Ajax.fetchAlbum, Ajax.deleteAlbum, Ajax.publish,
          Purchase.album, Purchase.track, Purchase.checkout

        )
      ).as("text/javascript")
  }

  def download = TransAction {
    WithArtist {
      artist => implicit request =>
        import utils.ZipCreator
        downloadForm.bindFromRequest.fold(
          errors => NotFound(errors.errorsAsJson),
          download => {
            val (file, name) = ZipCreator(artist, download, request)
            if (file.isDefined) Ok.sendFile(file.get, fileName = _ => name) else BadRequest("invalid_url")


          }
        )

    }


  }


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


  def sendForgottenPassword = TODO

  def forgotPassword = TODO

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

  def dashboard = authorizedAction(NormalUser) {
    artist => implicit request =>

      Redirect("http://google.com")
  }


  def changePassword = TODO

  def updatePassword = TODO

  def album(name: String) = TransAction {
    WithArtist {
      artist => implicit request =>

        Album.bySlug(artist.id, name).map(album =>
          Ok(html.display.album(artist, album, Album.withTracks(album.id).toList))
        ) getOrElse (NotFound("Album not found"))

    }
  }

  def page(name: String) = Action {
    implicit request =>
      Ok("name " + name)

  }

  def track(name: String) = Action {
    implicit request =>
      Ok("Track")
  }

}