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


  def index = authorizedAction(NormalUser) {
    artist => implicit request =>
      artist.activated match {
        case true => Ok(html.artist.index())
        case false => Redirect(routes.Artists.pickTags())
      }


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

  def addTrack = authorizedAction(NormalUser) {
    artist => implicit request =>
      Ok("add track")
  }

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
      request.method match {
        case "POST" => tagsForm.bindFromRequest.fold(
          errors => BadRequest(html.artist.pickTags(errors, Genres.allAsString)),
          value => {

            if (value.tags.isDefined) {
              models.Artists.insertTags(
                artist.id, value.tags.get.split(",").toList.map(_.trim)
              )
            }
            Ok("inserted tags")
          }

        )


        case "GET" => Ok(html.artist.pickTags(tagsForm, Genres.allAsString))
      }


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
