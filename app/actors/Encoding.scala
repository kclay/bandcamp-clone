package actors


import akka.actor.Actor
import java.io.File
import models.Artist
import utils.{AudioDataStore, ffmpeg}


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/28/12
 * Time: 9:27 AM
 */
//case class EncodingStatus(id: String, status: String)



case class Encode(id: String, session: String)

case class EncodeFailed(id: String, message: String)

case class VerifyRequest(id: String, file: File)

case class VerifyResponse(ok: Boolean)


class Encoding extends Actor {
  private val PREVIEW_LENGTH = 90
  lazy val audioDataStore = new AudioDataStore()

  def encode(id: String, session: String) {
    audioDataStore.tempFile(id).map {
      file => {
        val f = ffmpeg(file)
        val album = audioDataStore.album(session)
        album.mkdirs()
        val preview = new File(album, id + "_preview.mp3")
        val output = new File(album, id + "_full.mp3")
        val duration = f.duration

        if (duration > PREVIEW_LENGTH) {
          f.encode(preview, PREVIEW_LENGTH)
        }
      }
    }
  }

  protected def receive = {
    case Encode(id, session) => {

      encode(id, session)

    }

  }
}
