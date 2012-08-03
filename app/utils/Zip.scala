package utils

import models._
import utils.Assets._

import utils.Assets.audioStore
import play.api.mvc.RequestHeader

import java.io.File


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 8/3/12
 * Time: 10:48 AM
 */
abstract class Zip(artist: String) {

  import utils.Utils.normalize

  def file: Option[java.io.File] = None

  def url(implicit request: RequestHeader): String = {
    if (file.isEmpty) toZip
    audioStore.toURL(request.host, file.get)
  }

  def touch = if (file.isEmpty) toZip


  protected def toZip: Unit

  lazy val album: Option[Album] = None
  lazy val normalizedArtist = normalize(artist, " ")
  lazy val normalizedAlbum = normalize(album.get.name, " ")

  def format(index: Int, name: String) = "%s - %s - %02d %s.mp3".format(normalizedArtist, normalizedAlbum, index, name)


}


case class AlbumZip(artistID: Long, session: String, artist: String) extends Zip(artist) {

  override def file = audioStore.zip("album", session)

  override lazy val album = Album.bySession(artistID, session)

  def toZip = {
    album.map {
      a => {
        val tracks = Album.withTracks(a.id).map(t =>
          (t.name, audioStore.full(a.session, t.file.get))
        )
        audioStore.asZip(audioStore.zipFile("album", session), tracks)(format)
      }
    }
  }
}

case class TrackZip(artistID: Long, fileHash: String, artist: String) extends Zip(artist) {
  override def file = audioStore.zip("track", fileHash)

  private lazy val track = Track.byFile(artistID, fileHash)
  override lazy val album = AlbumTracks.withAlbum(track.get.id)

  override def toZip = {
    track.map {
      t =>

        audioStore.asZip(audioStore.zipFile("track", fileHash), List((t.name, audioStore.full(t.session, t.file.get))))(format)
    }
  }
}

object Zip {

  def apply(artist: String, kind: String, artistID: Long, sessionOrFile: String) =

    if (kind.equals("album")) new AlbumZip(artistID, sessionOrFile, artist) else TrackZip(artistID, sessionOrFile, artist)
}

object ZipCreator {


  def apply(artist: Artist, download: Download, request: RequestHeader): (Option[File], String) = {

    import org.squeryl.PrimitiveTypeMode.inTransaction

    inTransaction {
      if (download.valid(request)) {
        val zip = Zip(artist.name, download.kind, artist.id, download.item)
        zip.touch
        val fileName = "%s - %s.zip".format(zip.normalizedArtist, zip.normalizedAlbum)
        while (zip.file.isEmpty) {
          try {
            Thread.sleep(500)
          } catch {
            case _ =>
          }

        }
        (zip.file, fileName)
      } else {
        (None, "")
      }

    }
  }


}
