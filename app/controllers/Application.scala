package controllers


import actions.Actions._
import play.api._

import libs.Crypto
import play.api.mvc._
import views._
import models.Forms._
import jp.t2v.lab.play20.auth._
import models.SiteDB._
import actions.SquerylTransaction
import play.api.Play.current


import java.io.File
import models._
import play.api.Play


object Application extends Controller with Auth with MyLoginLogout with AuthConfigImpl with WithDB with SquerylTransaction {


  def Error(implicit request: RequestHeader) = {
    NotFound(html.notFound(request))
  }

  def javascriptRoutes = Action {
    implicit request =>
      import routes.javascript._
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          Upload.audio, Upload.art, Upload.audioUploaded, Upload.status,
          Ajax.fetchAlbum, Ajax.deleteAlbum, Ajax.deleteTrack, Ajax.publish, Ajax.fetchTrack,
          Stats.track, Stats.sales, Stats.plays,
          Purchase.album, Purchase.track, Purchase.checkout, Purchase.ajaxCommit


        )
      ).as("text/javascript")
  }


  def withDownload(info: Option[(String, String, String)]) =
    info.map {
      case (uri, name, contentType) => Ok.withHeaders("X-Accel-Redirect" -> uri)
        .withHeaders(CONTENT_DISPOSITION -> "attachment; filename=%s".format(name))
        .withHeaders(CONTENT_TYPE -> contentType)
    }.getOrElse(BadRequest)


  def download = TransAction {
    WithArtist {
      artist => implicit request =>

        downloadForm.bindFromRequest.fold(
          errors => NotFound(errors.errorsAsJson),
          download => download.withDownload(artist, withDownload)


        )


    }


  }


  def index = optionalUserAction {
    artist => implicit request =>

      if (hasSubdomain) Redirect(routes.Artists.index()) else Ok(html.index())


  }

  private def hasSubdomain(implicit request: RequestHeader): Boolean = {
    val parts = request.host.split("\\.")

    if (request.host.contains("localhost")) parts.size >= 2 else parts.size >= 3
  }


  def sendForgottenPassword = TransAction {
    Action {
      implicit request =>
        forgotForm.bindFromRequest.fold(
          errors => Ok(html.forgotPassword(errors)),
          artist => {


            val reset = PasswordReset(artist)

            val sent = Notification("BulaBowl reset password request", artist.email,
              html.email.passwordText(artist.name, reset.token).body)

            Ok(html.passwordSent())
          }

        )


    }
  }

  def resetPassword(token: String) = TransAction {
    Action {
      PasswordReset.find(token).map {
        r => Ok(html.resetPassword(resetForm))
      }.getOrElse(BadRequest)
    }
  }

  def updatePassword(token: String) = TransAction {
    Action {
      implicit request =>
        resetForm.bindFromRequest.fold(
          errors => Ok(html.resetPassword(errors)),
          value => {
            val (password, _) = value
            PasswordReset.find(token).map {
              r =>
                Artist.updatePassword(r.artistID, password)
                PasswordReset.delete(r.id)
                Ok(html.passwordChanged())
            }.getOrElse(BadRequest)
          }
        )

    }
  }

  def forgotPassword = Action {
    implicit request =>
      Ok(html.forgotPassword(forgotForm))
  }

  def createCodes = TransAction {
    implicit request =>

      val promos = for (i <- 1 to 10) yield Crypto.sign(i + "bula_code_promo" + System.nanoTime())
      codes.insert(for (c <- promos) yield PromoCode(c))
      Ok(promos.mkString("\n"))
  }

  def validateSignup = Action {
    implicit request =>
      db {
        signupFrom.bindFromRequest.fold(
          errors => BadRequest(html.signup(errors)),
          user => {
            val artist = artists insert Artist(user.username, user.password, user.email, user.name)


            PromoCode.delete(user.code)



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
      if (hasSubdomain) {
        import utils.Utils.domain

        Redirect("http://" + domain + "/login").withNewSession
      } else {
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


  }

  def logout = Action {
    implicit request =>
    /* if (hasSubdomain ) {
import utils.Utils.domain
gotoLogoutSucceeded
Redirect(domain + "/login").withNewSession
} else {    */
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
        ) getOrElse (BadRequest)

    }
  }

  def page(path: String, file: String) = Action {
    implicit request =>


      val resourceName = Option(path + file + ".html").map(name => if (name.startsWith("/")) name else ("/" + name)).get

      if (new File(resourceName).isDirectory || !new File(resourceName).getCanonicalPath.startsWith(new File(path).getCanonicalPath)) {
        NotFound
      } else {


        val resource = {
          Play.resource(resourceName + ".gz").map(_ -> true)
            .filter(_ => request.headers.get(ACCEPT_ENCODING).map(_.split(',').exists(_ == "gzip" && Play.isProd)).getOrElse(false))
            .orElse(Play.resource(resourceName).map(_ -> false))
        }
        resource.map {
          case (url, _) if new File(url.getFile).isDirectory => NotFound

          case (url, isGzipped) => {

            val content = scala.io.Source.fromFile(url.getFile).mkString
            Ok(html.page(file, content))
          }: Result
        }.getOrElse(NotFound)
      }

  }

  def track(name: String) = TransAction {
    WithArtist {
      artist => implicit request =>

        Track.bySlug(artist.id, name).map(track =>

          Ok(html.display.track(artist, AlbumTracks.withAlbum(track.id), track))
        ) getOrElse (BadRequest)

    }
  }

}