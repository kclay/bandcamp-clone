package actors


import akka.actor.Actor
import java.io.File
import models.Artist
import utils.{TempAudioDataStore, ffmpeg}


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/28/12
 * Time: 9:27 AM
 */
//case class EncodingStatus(id: String, status: String)



case class Encode(id: Long)

case class EncodeFailed(id: String, message: String)

case class VerifyRequest(id: String, file: File)

case class VerifyResponse(ok: Boolean)


class Encoding extends Actor {
  private val PREVIEW_LENGTH = 90
  lazy val audioDataStore = new TempAudioDataStore()

  def encode(queueId: Long) {
    import models.Queue;

    Queue.fetch(queueId).map {
      queue =>
        audioDataStore.tempFile(queue.file, queue.session).map {
          file => {
            Queue.updateStatus(queue.id, Queue.STATUS_PROCESSING)
            val f = ffmpeg(file)
            val album = audioDataStore.album(queue.session)
            album.mkdirs()
            val preview = audioDataStore.preview(album, queue.file)

            val output = audioDataStore.full(album, queue.file)
            val duration = f.duration


            if (!f.encode(preview, if (duration > PREVIEW_LENGTH) PREVIEW_LENGTH else 0)) {
              Queue.updateStatus(queue.id, Queue.STATUS_ERROR_PREVIEW);
              return
            }

            if (!f.encode(output)) {
              Queue.updateStatus(queue.id, Queue.STATUS_ERROR_FULL);
              preview.delete()
              return
            }
            Queue.updateStatus(queue.id, Queue.STATUS_COMPLETED, duration);

            file.delete()
            if (file.getParentFile.list().length == 0) file.getParentFile.delete
          }
        }
    }
  }

  protected def receive = {
    case Encode(queueId) => {

      encode(queueId)

    }

  }
}
