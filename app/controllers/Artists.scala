package controllers


import jp.t2v.lab.play20.auth._
import play.api._
import actions.Actions._
import play.api.mvc._
import play.api.data._
import format.Formatter
import play.api.data.Forms._
import play.api.data.format.Formats._
import views._
import models._
import utils.format.Formats._


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/5/12
 * Time: 6:59 PM
 */


case class Welcome(genre: Int, tags: Option[String], location: Option[String])

object Artists extends Controller with Auth with AuthConfigImpl with DataTable
{


  def index = authorizedAction(NormalUser) {
    implicit artist => implicit request =>
      artist.activated match {
        case true => {
          if (withSubdomain) Ok(html.artist.index()) else Redirect(withDomain)

        }
        case false => Redirect(routes.Artists.pickTags())
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


  val singleTrackForm: Form[Track] = Form {
    mapping(
      "id" -> longNumber,
      "artist_id" -> longNumber,
      "name" -> text(minLength = 1, maxLength = 50),
      "download" -> boolean,
      "price" -> of[Double],
      "artist" -> optional(text),
      "art" -> optional(text),
      "lyrics" -> optional(text),
      "about" -> optional(text),
      "credits" -> optional(text),
      "isrc" -> optional(text(maxLength = 12)),
      "date" -> optional(sqlDate("MM-dd-yyyy"))
    )(Track.apply)(Track.unapply)
  }

  def addTrack = authorizedAction(NormalUser) {
    artist => implicit request =>
      Ok(html.artist.addTrack(singleTrackForm))
  }

  def insertTrack = authorizedAction(NormalUser) {
    artist => implicit request =>
      singleTrackForm.bindFromRequest.fold(
        errors => BadRequest(html.artist.addTrack(singleTrackForm)),
        value => Ok("hello")
      )

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


  val tagsForm = Form {
    mapping(
      "genre" -> number,
      "tags" -> optional(text),
      "location" -> optional(text)
    )(Welcome.apply)(Welcome.unapply)
  }


  def pickTags = authorizedAction(NormalUser) {
    artist => implicit request =>
      Ok(html.artist.pickTags(tagsForm, Genres.allAsString))


  }

  def insertTags = authorizedAction(NormalUser) {
    artist => implicit request =>
      tagsForm.bindFromRequest.fold(
        errors => BadRequest(html.artist.pickTags(errors, Genres.allAsString)),
        value => {

          if (value.tags.isDefined) {
            models.Artists.insertTags(
              artist.id, value.tags.get.split(",").toList.map(_.trim)
            )
          }
          Redirect(routes.Artists.pickDomain)
        })
  }


  val domainForm = Form(
    single(
      "domain" -> text(minLength = 4, maxLength = 25)
    ) verifying(
      "Domain already taken", result => result match {
      case (domain) => models.Artists.findByDomain(domain).isEmpty
    })
  )


  def pickDomain = authorizedAction(NormalUser) {
    artist => implicit request =>
      val defaultDomain = artist.name.replace(" ", "").toLowerCase
      request.method match {
        case "POST" => domainForm.bindFromRequest.fold(
          formWithErrors => BadRequest(html.artist.pickDomain(domainForm)),
          value => {

            models.Artists.updateDomain(artist.id, value)
            Redirect(routes.Artists.index)

          }
        )
        case "GET" => Ok(html.artist.pickDomain(domainForm.fill(defaultDomain)))
      }

  }

}
