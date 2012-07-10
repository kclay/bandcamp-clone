package controllers

import jp.t2v.lab.play20.auth._
import models.AuthConfigImpl
import models.NormalUser
import play.api.mvc._
import com.codahale.jerkson.Json._
import akka.actor.Props
import play.api.libs.concurrent.AkkaPromise

import actors._
import akka.util.Timeout

// Use the Applications Default Actor System

import play.libs.Akka.system
import utils.AudioDataStore

// Necessary for `actor ? message`

import akka.pattern.ask


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/28/12
 * Time: 9:40 AM
 */

case class ArtResponse(created: Boolean, url: String, id: String)

case class AudioResponse(created: Boolean)

import org.apache.commons.codec.digest.DigestUtils._

object Upload extends Controller with Auth with AuthConfigImpl {


  def status(ids: String) = authorizedAction(NormalUser) {
    artist => implicit request =>
    /*
      Async {
        implicit val timeout = Timeout(5.seconds)
        new AkkaPromise(encodingActor ? EncodingStatus(ids.split(","))) map {

        }
      }   */
      Ok("")

  }

  lazy val audioDataStore = new AudioDataStore()

  val encodingActor = system.actorOf(Props[Encoding], name = "upload")

  private def decryptToken(token: String): Option[User] = {
    import play.api.libs.Crypto
    val splitted = token.split("-")
    val id = splitted.tail.mkString("-")
    if (splitted(0) == Crypto.sign(id))
      resolveUser(id.asInstanceOf[this.Id])
    else
      None
  }


  def audio(token: String) = Action(parse.multipartFormData) {
    implicit request =>
      decryptToken(token).map {
        artist => {

          request.body.file("Filedata").map {
            file =>
              val (created, name, tempFile) = audioDataStore.moveToTemp(file)

              if (created) {

                /*Async {
                  val id = shaHex(tempFile.getAbsolutePath)

                  encodingActor ? Encode(artist, id, tempFile)
                } */

              }
          }
        }
        Ok("")
      }
      Ok("")
  }

  def art(token: String) = Action(parse.multipartFormData) {
    implicit request =>
      decryptToken(token).map {
        artist => {
          request.body.file("Filedata").map {
            file =>
              import utils.{Medium, Image}

              val image = Image(file).resizeTo(Medium())


              Ok(generate(ArtResponse(image.exists(), image.url, image.id)))

          }.getOrElse(BadRequest("error"))
        }
      }
      Ok("")
  }

}
