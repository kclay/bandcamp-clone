package controllers


import jp.t2v.lab.play20.auth._
import play.api.mvc._

import views._
import models._
import models.Forms._
import com.codahale.jerkson.Json._
import models.Forms.domainForm
import models.Artist
import utils.AudioDataStore

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/5/12
 * Time: 6:59 PM
 */


object Artists extends Controller with Auth with AuthConfigImpl with WithDB {


  def index = authorizedAction(NormalUser) {
    implicit artist => implicit request =>
      artist.activated match {
        case true => {
          if (withSubdomain) Ok(html.artist.index()) else Redirect(withDomain)

        }
        case false => Redirect(if (Artist.hasTag(artist.id)) routes.Artists.pickDomain() else routes.Artists.pickTags())
      }


  }

  private def withDomain(implicit artist: Artist, request: RequestHeader): String = {


    "http://" + artist.domain + "." + request.host + "/welcome"
  }

  private def withSubdomain(implicit request: RequestHeader): Boolean = {
    val parts = request.host.split("\\.")

    parts.size >= 2
  }

  def editAlbum(name: String) = authorizedAction(NormalUser) {
    artist => implicit request =>
      Ok("edit album")
  }

  def editTrack(name: String) = authorizedAction(NormalUser) {
    artist => implicit request =>
      Ok("edit track")
  }


  def editAlbum = authorizedAction(NormalUser) {
    implicit artist => implicit request =>

      Ok(html.artist.newAlbum(albumForm.fill(Album(), Seq.empty[Track])))
  }


  def addTrack = authorizedAction(NormalUser) {
    implicit artist => implicit request =>
      Ok(html.artist.addTrack(singleTrackForm))
  }

  def insertTrack = authorizedAction(NormalUser) {
    implicit artist => implicit request =>

      singleTrackForm.bindFromRequest.fold(
        errors => BadRequest(""),
        track => {

          Ok("") //generate(track.save()))
        }
      )

  }

  def publishTrack(id: Long) = authorizedAction(NormalUser) {
    artist => implicit request =>
      Track.publish(id)
      Ok("")
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
          tags.map(t => ArtistTag.insert(artist, t.split(",").toList.map(_.trim)));


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

}
