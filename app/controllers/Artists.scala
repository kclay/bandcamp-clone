package controllers


import jp.t2v.lab.play20.auth._
import play.api._
import actions.Actions._
import play.api.mvc._
import play.api.data._
import format.Formatter

import views._
import models._
import models.Forms._
import com.codahale.jerkson.Json._
import models.Forms.domainForm
import models.Artist


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/5/12
 * Time: 6:59 PM
 */


object Artists extends Controller with Auth with AuthConfigImpl with WithDB
{


  def index = authorizedAction(NormalUser) {
    implicit artist => implicit request =>
      artist.activated match {
        case true => {
          if (withSubdomain) Ok(html.artist.index()) else Redirect(withDomain)

        }
        case false => Redirect(if (Artist.hasTag(artist.id)) routes.Artists.pickDomain() else routes.Artists.pickTags())
      }


  }

  private def withDomain(implicit artist: Artist, request: RequestHeader): String =
  {


    "http://" + artist.domain + "." + request.host
  }

  private def withSubdomain(implicit request: RequestHeader): Boolean =
  {
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

  def addAlbum = authorizedAction(NormalUser) {
    artist => implicit request =>
      Ok("add album")
  }


  def upload(kind: String) = Action(parse.multipartFormData) {
    implicit request =>

      request.body.file("Filedata").map {
        file =>
          import utils._


          var response: String = ""
          kind match {
            case "audio" => {
              val (created, name) = AudioDataStore("audio").moveToTemp(file)
              response = created.toString + "|" + name
            }
            case "art" => {

              val image = Image(file).resizeTo(Medium())
              if (image.exists()) {
                response = String.format("true|%s|%s", image.url, image.id)
              } else {
                response = "error";
              }

            }
          }
          Ok(response)

      }.getOrElse {
        Redirect(routes.Application.index).flashing(
          "error" -> "Missing file"
        )
      }
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

  def fetchTrack(id: Long) = authorizedAction(NormalUser) {
    artist => implicit request =>

      Ok(Track.find(id).map {
        case t => if (t.artistID == artist.id) generate(t) else ""
      }.getOrElse(""))


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
