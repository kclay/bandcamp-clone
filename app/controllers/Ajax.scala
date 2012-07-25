package controllers

import play.api._
import play.api.mvc._
import models._
import models.Forms._
import com.codahale.jerkson.Json._
import jp.t2v.lab.play20.auth.Auth


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/11/12
 * Time: 11:18 AM
 */

object Ajax extends Controller with Auth with AuthConfigImpl with WithDB {


  def tags(query: String) = Action {
    val found = db {
      Tag.search(query).map {
        case (t) => Map("name" -> t.name)
      }
    }


    Ok(generate(found))
  }

  def saveTrack() = authorizedAction(NormalUser) {
    implicit artist => implicit request =>
      singleTrackForm.bindFromRequest.fold(
        errors => BadRequest(""),
        track => {

          Ok("") //generate(track.save()))
        }
      )
  }

  def fetchTrack(id: Long) = authorizedAction(NormalUser) {
    artist => implicit request =>
      db {
        Ok(Track.find(id).map {
          case t => if (t.artistID == artist.id) generate(t) else ""
        }.getOrElse(""))
      }


  }

  def fetchAlbum(id: Long) = authorizedAction(NormalUser) {
    artist => implicit request =>
      Ok(Album.forArtist(artist.id, id).map {
        case a => generate(Map('album -> a, 'tracks -> Album.withTracks(a.id)))
      }.getOrElse(""))

  }

  def saveAlbum() = authorizedAction(NormalUser) {
    implicit artist => implicit request =>
      albumForm.bindFromRequest.fold(
        errors => BadRequest(errors.errorsAsJson),
        value => {
          val (album, tracks) = value
          Ok(generate(Map('album -> album, 'tracks -> tracks)))

        }
      )
  }


}
