package controllers

import play.api._
import play.api.mvc._
import models._
import models.Forms._
import com.codahale.jerkson.Json._
import jp.t2v.lab.play20.auth.Auth
import org.squeryl.PrimitiveTypeMode
import actions.{Authorizer, SquerylTransaction}
import java.io.{File, FileFilter}
import scala.Some
import models.AlbumTracks
import services.PayPal
import utils.TempImage._
import scala.Some
import utils.TempAudioDataStore._
import scala.Some
import actions.Actions._
import scala.Some
import models.Tag._


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/11/12
 * Time: 11:18 AM
 */

object Ajax extends Controller with Auth with AuthConfigImpl with WithDB with SquerylTransaction with Authorizer {

  import models.SiteDB._
  import PrimitiveTypeMode._


  def tags(query: String) = TransAction {
    Action {
      val found =
        Tag.search(query).map {
          case (t) => Map("name" -> t.name)
        }



      Ok(generate(found))
    }
  }

  def insertAlbumTags(artist: Artist, albums: List[(String, List[String])]) = {

    val album = albums(0)
    Album.bySlug(artist.id, album._1).map {
      a => Tag.insert(a, album._2)
    }
    albums.drop(1).map {
      track =>
        for {
          t <- Track.bySlug(artist.id, track._1)
        } yield Tag.insert(t, track._2)
    }
  }

  def insertTrackTags(artist: Artist, tracks: List[(String, List[String])]) {

    tracks.foreach(track =>
      for {
        t <- Track.bySlug(artist.id, track._1)
      } yield (Tag.insert(t, track._2))
    )

  }

  def saveTags = Authorize {
    artist => implicit request =>

      saveTagsForm.bindFromRequest.fold(
        errors => BadRequest(errors.errorsAsJson),
        value => {
          val (kind, items) = value
          if (kind == "album") insertAlbumTags(artist, items) else insertTrackTags(artist, items)
          Ok("")
        }
      )
  }

  def saveTrack() = Authorize {

    implicit artist => implicit request =>
      commitTrack(artist)

  }

  def updateTrack(slug: String) = Authorize {
    implicit artist => implicit request =>
      commitTrack(artist)

  }

  def commitTrack(artist: Artist)(implicit request: play.api.mvc.Request[_]): play.api.mvc.Result = {
    singleTrackForm.bindFromRequest.fold(
      errors => BadRequest(errors.errorsAsJson),
      track => {

        // select all slugs
        val slugs = from(tracks)(t =>
          where(t.artistID === artist.id)
            select(t.slug, t.id)
        ).toMap

        var counter = 1
        val slug = track.slug


        while (slugs.contains(track.slug) && slugs.getOrElse(track.slug, -1) != track.id) {
          track.slug = slug + "-" + counter
          counter += 1
        }

        val activeHashes = List(track.art.getOrElse(""), track.file.getOrElse(""))

        // update or delete album
        track.single = true
        if (track.id == 0) track.save else track.update

        // compose our filter that checks for active file hashes(art and mp3) for the giving session
        val commitFileFilter = new FileFilter() {
          def accept(file: File) = file.getName.split("_").headOption.map(f => activeHashes.contains(f)).getOrElse(false)
        }
        commitImagesForSession(track.session, Some(commitFileFilter))
        commitAudioForSession(track.session, Some(commitFileFilter))

        track.rebuild
        Ok(generate(Map("track" -> track)))
      }
    )

  }

  def fetchTrack(slug: String) = Authorize {
    artist => implicit request =>

      Ok(Track.bySlug(artist.id, slug).map {
        case t => generate(Map("track" -> t))
      }.getOrElse("")).as("text/json")


  }

  def publish(kind: String, slug: String) = Authorize {
    implicit artist => implicit request =>

      kind match {
        case "album" => Album.bySlug(artist.id, slug).map {
          album =>
            update(albums)(a =>
              where(a.id === album.id)
                set (a.active := true))

            update(tracks)(t =>
              where(t.id in
                from(Album.withTracks(album.id))(t2 => select(t2.id))
              )
                set (t.active := true)
            )


        }
        case "track" => Track.bySlug(artist.id, slug).map {
          track => update(tracks)(t =>
            where(t.id === track.id)
              set (t.active := true))
        }
      }
      Ok("")


  }

  def fetchAlbum(slug: String) = Authorize {
    artist => implicit request =>
      Ok(Album.bySlug(artist.id, slug).map {
        a => generate(Map("album" -> a, "tracks" -> Album.withTracks(a.id).toList))
      }.getOrElse("")).as("text/json")


  }

  private def g(obj: Any) = generate(obj)

  private def json(obj: Any) = Ok(g(obj)).as("text/json")

  private def error(obj: Any) = BadRequest(g(obj)).as("text/json")

  def deleteAlbum(slug: String) = Authorize {
    artist => implicit request =>

      Album.bySlug(artist.id, slug).map {
        album =>
          import utils.AudioDataStore.deleteAudioSession
          deleteAudioSession(album.session)
          albums.delete(album.id)
          val allTracks = from(albumTracks)(at =>
            where(at.albumID === album.id)
              select (at)

          )
          tracks.deleteWhere(t =>
            (t.id in from(allTracks)(at => select(at.trackID)))

          )
          albumTracks.delete(allTracks)
          json(Map("ok" -> true))

      }.getOrElse(error(Map("ok" -> false)))

  }

  def deleteTrack(slug: String) = Authorize {
    artist => implicit request =>
      Track.bySlug(artist.id, slug).map {
        track =>
          import utils.AudioDataStore.deleteAudioSession
          deleteAudioSession(track.session)
          tracks.delete(track.id)
          json(Map("ok" -> true))
      }.getOrElse(error(Map("ok" -> false)))

  }

  def updateAlbum(slug: String) = Authorize {
    implicit artist => implicit request =>

      commitAlbum(artist)

  }

  def saveAlbum() = Authorize {
    implicit artist => implicit request =>

      commitAlbum(artist)


  }

  private def commitAlbum(artist: Artist)(implicit request: play.api.mvc.Request[_]): play.api.mvc.Result = {

    albumForm.bindFromRequest.fold(
      errors => BadRequest(errors.errorsAsJson),
      value => {

        import utils.TempImage.commitImagesForSession
        import utils.TempAudioDataStore.commitAudioForSession
        import utils.Assets.tempAudioStore
        import utils.ffmpeg

        var (album, allTracks) = value

        val albumSlugs = from(albums)(a =>
          where(a.artistID === artist.id)
            select(a.slug, a.id)
        ).toMap






        val albumSlug = album.slug
        var counter = 2
        while (albumSlugs.contains(album.slug) && albumSlugs.getOrElse(album.slug, -1) != album.id) {
          album.slug = albumSlug + "-" + counter
          counter += 1
        }

        // update or delete album
        if (album.id == 0) album.save else album.update

        // select all slugs
        var slugs = from(tracks)(t =>
          where(t.artistID === artist.id)
            select(t.slug, t.id)
        ).toMap

        allTracks.foreach {
          t =>
            var counter = 1
            val slug = t.slug

            while (slugs.contains(t.slug) && slugs.getOrElse(t.slug, -1) != t.id) {
              t.slug = slug + "-" + counter
              counter += 1
            }

            // update active slug list just in case a user inputs the same title twice in
            // the same update session

            if (slug != t.slug) slugs ++= Map(t.slug -> t.id)
        }


        // update the tracks
        tracks.update(allTracks.filter(_.id != 0))



        // save all the new tracks
        allTracks.foreach(t => if (t.id == 0) t.save)



        // all tracks have their id now, so create a map of all ideas
        val currentTrackIds = allTracks.map(_.id)

        // create a list of active hashs from the art and file attribute,
        // this will allow us to only copy files that are being saved,
        // so if the user deletes a file before a save we don't worry about saving that one,
        // this will prevent any false overwritting of files
        val activeHashes = List(album.art.getOrElse("")) ++ allTracks.map(_.file.getOrElse("")) ++ allTracks.map(_.art.getOrElse(""))

        // go ahead and delete files that were not send over with this save
        // TODO: maybe this should clean up on the delete files
        tracks.deleteWhere(t => t.id notIn currentTrackIds and t.artistID === artist.id)



        // delete all the album order information and reinsert
        albumTracks.delete(albumTracks.where(at => at.albumID === album.id))
        var order = 0

        albumTracks.insert(allTracks.map(t => {
          order += 1
          AlbumTracks(album.id, t.id, order)
        }))

        // compose our filter that checks for active file hashes(art and mp3) for the giving session
        val commitFileFilter = new FileFilter() {
          def accept(file: File) = {

            val answer = file.getName.split("_").headOption.map(f => activeHashes.contains(f)).getOrElse(false)
            answer
          }
        }
        commitImagesForSession(album.session, Some(commitFileFilter))
        commitAudioForSession(album.session, Some(commitFileFilter))




        album.rebuild
        allTracks.map(_.rebuild)
        Ok(generate(Map("album" -> album, "tracks" -> allTracks)))

      }
    )
  }
}



