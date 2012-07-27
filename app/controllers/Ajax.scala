package controllers

import play.api._
import play.api.mvc._
import models._
import models.Forms._
import com.codahale.jerkson.Json._
import jp.t2v.lab.play20.auth.Auth
import org.squeryl.PrimitiveTypeMode
import actions.SquerylTransaction


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/11/12
 * Time: 11:18 AM
 */

object Ajax extends Controller with Auth with AuthConfigImpl with WithDB with SquerylTransaction {

  import models.SiteDB._
  import PrimitiveTypeMode._

  def tags(query: String) = Action {
    val found = db {
      Tag.search(query).map {
        case (t) => Map("name" -> t.name)
      }
    }


    Ok(generate(found))
  }

  def saveTrack() = authorizedAction(NormalUser) {
    implicit artist => implicit request =>
      singleTrackForm.bindFromRequest.fold(
        errors => BadRequest(""),
        track => {

          Ok("") //generate(track.save()))
        }
      )
  }

  def fetchTrack(id: Long) = authorizedAction(NormalUser) {
    artist => implicit request =>
      db {
        Ok(Track.find(id).map {
          case t => if (t.artistID == artist.id) generate(t) else ""
        }.getOrElse(""))
      }


  }

  def publish(kind: String, id: Long) = authorizedAction(NormalUser) {
    implicit artist => implicit request =>

      kind match {
        case "album" => Album.forArtist(artist.id, id).map {
          case a => {
            update(albums)(a =>
              where(a.id === id)
                set (a.active := true))

            update(tracks)(t =>
              where(t.id in
                from(Album.withTracks(a.id))(t2 => select(t2.map(_.id)))
              )
                set (t.active := true)
            )

          }
        }
        case "track" =>
      }
      Ok("")

  }

  def fetchAlbum(slug: String) = TransAction {
    authorizedAction(NormalUser) {
      artist => implicit request =>
        Ok(Album.bySlug(artist.id, slug).map {
          case a => generate(Map("album" -> a, "tracks" -> Album.withTracks(a.id).toList))
        }.getOrElse(""))

    }
  }


  def saveAlbum() = authorizedAction(NormalUser) {
    implicit artist => implicit request =>

      albumForm.bindFromRequest.fold(
        errors => BadRequest(errors.errorsAsJson),
        value => {

          import utils.TempImage.commit
          var (album, allTracks) = value
          db {
            // update or delete album
            if (album.id == 0) album.save else album.update


            // filter out tracks that have been saved already
            val updates = allTracks.filter(_.id != 0)
            // TODO : Update slugs
            // save all the new tracks
            allTracks.foreach(t => if (t.id == 0) t.save)

            // update the tracks
            tracks.update(updates)

            val currentTrackIds = allTracks.map(_.id)

            tracks.deleteWhere(t => t.id notIn currentTrackIds and t.artistID === artist.id)



            albumTracks.delete(albumTracks.where(at => at.albumID === album.id))
            var order = 0

            albumTracks.insert(allTracks.map(t => {
              order += 1
              AlbumTracks(album.id, t.id, order)
            }))


            commit(album.session)


          }
          /*
         val updates = items.collect {
           case Right(t) => t
         }
         tracks.update(updates) */

          Ok(generate(Map('album -> album, 'tracks -> allTracks)))

        }
      )
  }
}


