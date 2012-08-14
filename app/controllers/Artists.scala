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
import actions.Actions._
import utils.AudioDataStore

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
          track => Ok(html.artist.addTrack(singleTrackForm.fill(track)))
        }.getOrElse(BadRequest)

    }
  }


  def newAlbum = Authorize  {
    implicit artist => implicit request =>

      Ok(html.artist.albumView(albumForm.fill(Album(), Seq.empty[Track])))
  }

  def editAlbum(name: String) = TransAction {
    Authorize{
      implicit artist => implicit request =>
        Album.bySlug(artist.id, name).map {
          a =>
            Ok(html.artist.albumView(albumForm.fill(a, Seq.empty[Track])))
        }.getOrElse(BadRequest)
    }
  }


  def newTrack =  Authorize {
    implicit artist => implicit request =>
      Ok(html.artist.addTrack(singleTrackForm.fill(Track())))
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


  def pickTags =  Authorize {
    artist => implicit request =>
      Ok(html.artist.pickTags(tagsForm, db(Genre.allAsString)))


  }

  def insertTags =  Authorize {
    artist => implicit request =>
      tagsForm.bindFromRequest.fold(
        errors => BadRequest(html.artist.pickTags(errors, Genre.allAsString)),
        value => {

          val (genre, tags, location) = value
          tags.map(t => ArtistTag.insert(artist, t.split(",").toList.map(_.trim)));


          Redirect(routes.Artists.pickDomain)
        })
  }

  def insertDomain =  Authorize {
    artist => implicit request =>

      domainForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.artist.pickDomain(domainForm)),
        value => {

          Artist.updateDomain(artist.id, value)
          Redirect(routes.Artists.index)

        }
      )


  }

  def pickDomain =  Authorize {
    artist => implicit request =>
      val defaultDomain = artist.name.replace(" ", "").toLowerCase
      Ok(html.artist.pickDomain(domainForm.fill(defaultDomain)))


  }

}
