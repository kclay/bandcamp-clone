package controllers

import jp.t2v.lab.play20.auth._
import models.{Queue, AuthConfigImpl, NormalUser}
import play.api.mvc._
import com.codahale.jerkson.Json._
import akka.actor.Props
import play.api.libs.concurrent.{Akka, AkkaPromise}


import actors._


import akka.util.{Duration, Timeout}
import com.ning.http.util.Base64
import play.api.libs.Files.TemporaryFile


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

import org.apache.commons.codec.digest.DigestUtils._
import play.api.libs.Crypto

case class ArtResponse(ok: Boolean, url: String = "", id: String = "")

case class UploadError(error: String)

case class AudioResponse(name: String, id: String)

case class QueueAdded(id: Long) {
  def hash = Base64.encode(Crypto.sign(id.toString).getBytes)
}


object Upload extends Controller with Auth with AuthConfigImpl {


  def status() = Action {
    implicit request =>
      import models.Forms.trackStatusForm
      import java.lang.{Long => JLong}
      trackStatusForm.bindFromRequest.fold(
        e => error("no_ids"),
        value => {
          val ids = value.map {
            v =>
              val splitted = v.split("-")
              val id = splitted.tail.mkString("-")
              splitted(0) == Crypto.sign(id) match {
                case true => JLong.parseLong(id)
                case false =>
              }
          }.asInstanceOf[List[Long]]

          if (ids.nonEmpty) {
            val status = Queue.status(ids)

            status.foreach {
              case (id, s) => if (s.equals(Queue.STATUS_COMPLETED)) Queue.delete(id) else None
            }
            json(Map("encodings" -> status))
          } else error("no_ids")
        }

      )


  }

  lazy val audioDataStore = new AudioDataStore()

  val encodingActor = system.actorOf(Props[Encoding], name = "upload")
  private val noUser: Option[User] = None

  private def urldecode(data: String) = java.net.URLDecoder.decode(data, "UTF-8").split("\u0000").map(_.split(":")).map(p => p(0) -> p.drop(1).mkString(":")).toMap

  private def decryptToken()(implicit request: Request[MultipartFormData[TemporaryFile]]): Option[User] = {

    implicit val r = request.asInstanceOf[RequestHeader]
    import models.Forms.authTokenForm
    import java.lang.{Long => JLong}
    authTokenForm.bindFromRequest.fold(
      error => None,
      token => {
        var chars = Base64.decode(token)

        val splitted = new String(chars).split("-")
        val message = splitted.tail.mkString("-")
        splitted(0) == Crypto.sign(message) match {
          case true => urldecode(message).get("id").map(id => resolveUser(JLong.parseLong(id))).getOrElse(None)
          case false => noUser


        }
      }
    )

  }

  private val QUEUE_COUNTER_OFFSET = 18921;

  private def g(obj: Any) = generate(obj)

  private def json(obj: Any) = Ok(g(obj)).as("text/json")

  private def error(msg: String) = BadRequest(g(UploadError(msg))).as("text/json")


  def audioUploaded = authorizedAction(NormalUser) {
    artist => implicit request =>
      import models.Forms.trackUploadedForm
      trackUploadedForm.bindFromRequest.fold(
        e => error("error"),
        value => {
          val (id, session) = value
          audioDataStore.tempFile(id,session).map {
            file =>
              import utils.ffmpeg
              val errors = ffmpeg(file).verify
              if (errors.isEmpty) {
                val queue = Queue.add(id, session)

                import akka.util.duration
                import play.api.Play.current
                Akka.system.scheduler.scheduleOnce(Duration("2 seconds"), encodingActor, Encode(queue))
                json(Map("id" -> "%s-%d".format(Crypto.sign(queue.toString), queue)))
              } else json(Map("error" -> errors))

          }.getOrElse(error("invalid"))

        }


      )


  }

  def audio = Action(parse.multipartFormData) {
    implicit request =>

      decryptToken.map {
        artist =>

          request.body.file("Filedata").map {
            file =>
              import models.Forms.idSessionForm
              val (id, session) = idSessionForm.bindFromRequest.get
              val (created, name, tempFile) = audioDataStore.toTempMaybeId(id,file,session, false)

              if (created) json(AudioResponse(file.filename, name)) else None


          }.getOrElse(error("no_file"))


      }.getOrElse(error("invalid_token")).asInstanceOf[Result]
  }

  def art = Action(parse.multipartFormData) {
    implicit request =>
      decryptToken.map {
        artist => {
          request.body.file("Filedata").map {
            file =>
              import utils.{Medium, TempImage}
              import models.Forms.idSessionForm
              val (id, session) = idSessionForm.bindFromRequest.get

              // check if replacing previous uploaded image
              val image = (if (id.nonEmpty) TempImage(id.get, session, file) else TempImage(file, session)).resizeTo(Medium())


              json(ArtResponse(image.exists, image.url, image.id))

          }.getOrElse(error("error"))
        }
      }.getOrElse(error("error"))

  }

}
