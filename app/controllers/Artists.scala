package controllers


import jp.t2v.lab.play20.auth._
import play.api.mvc._

import views._
import models._
import models.Forms._
import com.codahale.jerkson.Json._
import models.Forms.domainForm
import models.Artist
import actions._

import utils.{Utils, AudioDataStore}

import models.ProfileInfo
import scala.Some
import play.api.data.Form
import play.api.data.Forms._
import models.ProfileInfo
import scala.Some
import org.squeryl.PrimitiveTypeMode

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/5/12
 * Time: 6:59 PM
 */


object Artists extends Controller with Auth with AuthConfigImpl with WithDB with SquerylTransaction with Authorizer {


  def index = optionalUserAction {
    implicit a => implicit request =>
      (for {
        artist <- a
        r <- withLogin(artist, request)
      } yield r).getOrElse(withViewer)


  }

  def stats = Authorize {
    implicit artist => implicit request =>
      Ok(html.artist.stats())
  }

  private def withViewer(implicit request: RequestHeader) = {
    import utils.Utils.withArtist
    (for {
      artist <- withArtist(request)
    } yield Ok(html.artist.items(artist, Artist.withAlbums(artist.id))))
      .getOrElse(BadRequest)

  }

  private def withLogin(implicit artist: Artist, request: RequestHeader) = {
    if (artist.activated) {

      Some(if (withSubdomain) Ok(html.artist.index()) else Redirect(withDomain))

    } else {
      Some(Redirect(if (Artist.hasTag(artist.id)) routes.Artists.pickDomain() else routes.Artists.pickTags()))
    }
  }

  private def withDomain(implicit artist: Artist, request: RequestHeader): String = {


    "http://" + artist.domain + "." + request.host + "/welcome"
  }

  private def withSubdomain(implicit request: RequestHeader): Boolean = {
    val parts = request.host.split("\\.")

    if (request.host.contains("localhost")) parts.size >= 2 else parts.size >= 3
  }


  def editTrack(name: String) = TransAction {
    Authorize {
      implicit artist => implicit request =>
        Track.bySlug(artist.id, name).map {
          track => Ok(html.artist.addTrack(singleTrackForm.fill(track),Genre.allAsString))
        }.getOrElse(BadRequest)

    }
  }


  def newAlbum = Authorize {
    implicit artist => implicit request =>

      Ok(html.artist.albumView(albumForm.fill(Album(), Seq.empty[Track]),Genre.allAsString))
  }

  def editAlbum(name: String) = TransAction {
    Authorize {
      implicit artist => implicit request =>
        Album.bySlug(artist.id, name).map {
          a =>
            Ok(html.artist.albumView(albumForm.fill(a, Seq.empty[Track]),Genre.allAsString))
        }.getOrElse(BadRequest)
    }
  }


  def newTrack = Authorize {
    implicit artist => implicit request =>
      Ok(html.artist.addTrack(singleTrackForm.fill(Track()),Genre.allAsString))
  }


  /*private def insertTrack(track: Track) =
 {
  db withSession {
    implicit s =>
      Tracks insert (Track)
  }
 } */

  def list(page: Int, amount: Int, query: String = "") = Action {
    Ok("artists.list")
  }

  def albums(page: Int, amount: Int) = TransAction {
    Authorize {
      artist => implicit request =>
        Ok(html.artist.albums(Album.forArtist(artist.id, page, amount)))
    }
  }

  def tracks(page: Int, amount: Int) = TransAction {
    Authorize {
      artist => implicit request =>
        Ok(html.artist.tracks(Track.withSingle(artist.id, page, amount)))
    }
  }


  def pickTags = authorizedAction(NormalUser) {
    artist => implicit request =>
      Ok(html.artist.pickTags(tagsForm, db(Genre.allAsString)))


  }

  def insertTags = authorizedAction(NormalUser) {
    artist => implicit request =>
      tagsForm.bindFromRequest.fold(
        errors => BadRequest(html.artist.pickTags(errors, Genre.allAsString)),
        value => {

          val (genre, tags, location) = value
          import models.Tag._
          Artist.updateGenre(artist.id, genre)
          tags.map(t => Tag.insert(artist.asInstanceOf[Artist], t.split(",").toList));


          Redirect(routes.Artists.pickDomain)
        })
  }

  def insertDomain = authorizedAction(NormalUser) {
    artist => implicit request =>

      domainForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.artist.pickDomain(domainForm)),
        value => {

          Artist.updateDomain(artist.id, value)
          Redirect(routes.Artists.index)

        }
      )


  }

  def pickDomain = authorizedAction(NormalUser) {
    artist => implicit request =>
      val defaultDomain = artist.name.replace(" ", "").toLowerCase
      Ok(html.artist.pickDomain(domainForm.fill(defaultDomain)))


  }


  def profileForm(implicit artist: Artist) = Form(
    mapping(

      "username" -> text(minLength = 6)
        .verifying("Invalid username", {
        !Seq("admin", "guest").contains(_)
      }).
        verifying("This username is not available", {
        username => username == artist.username || models.Artist.findByUsername(username).isEmpty
      }),
      "passwords" -> tuple(
        "password" -> text,
        "confirm_password" -> text
      ).verifying(
        // Add an additional constraint: both passwords must match
        "Passwords don't match", passwords => passwords._1 == passwords._2
      ),
      "email" -> email.verifying("This email has already been registered", {
        e => e == artist.email || models.Artist.byEmail(e).isEmpty
      }),

      "name" -> text(minLength = 1, maxLength = 100)


    ) {

      (username, password, email, name) => ProfileInfo(username, password._1, email,  name)
    } {
      s => Some(s.username, (s.password, s.password), s.email, s.name)
    }

  )

  def updateProfile = Authorize {
    implicit artist => implicit request =>
      profileForm(artist).bindFromRequest.fold(
        hasErrors => Ok(withProfile(artist)),
        profile => {

          import models.SiteDB._
          import PrimitiveTypeMode._

          update(artists)(
            a => where(a.id === artist.id)
              set(a.username := profile.username, a.email := profile.email,
               a.name := profile.name
             )

          )
          if (profile.password.nonEmpty) Artist.updatePassword(artist.id, profile.password)
          Redirect(routes.Artists.displayProfile()).flashing("success" -> "Profile Infomation updated")
        }

      )
  }

  private def withProfile(artist: Artist)(implicit request: RequestHeader) = html.artist.profile(artist,
    profileForm(artist).fill(ProfileInfo(artist.username, "", artist.email,  artist.name)),
    Genre.allAsString
  )

  def displayProfile = Authorize {
    implicit artist => implicit request =>
      Ok(withProfile(artist))
  }

}
