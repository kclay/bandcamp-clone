package controllers

import actions.SquerylTransaction

import play.api.mvc._
import models.SiteDB._
import org.squeryl.PrimitiveTypeMode._
import models.GameScene
import utils.Utils
import com.codahale.jerkson.Json._
import play.api.cache.Cache

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 9/17/12
 * Time: 12:21 PM 
 */
object Api extends Controller with SquerylTransaction {


  def fetch(tags: String, page: Int, amount: Int) = TransAction {
    Action {
      val domain = Utils.domain
      val rs = join(tracks, artists)((t, a) =>
        where(a.genreID in from(genres)(
          g => where(g.tag in tags.split(",").toSeq)
            select (g.id)
        ))
          select(t, a)
          on (t.artistID === a.id)

      ).page((page - 1) * amount, amount).map {
        r =>
          val (track, artist) = r

          Map("file" -> track.previewURL(domain),
            "title" -> track.name,
            "duration" -> track.duration,
            "link" -> track.url(domain),
            "image" -> track.artURL,
            "artist" -> Map(
              "name" -> artist.name,
              "link" -> "http://%s.%s".format(artist.domain, domain)
            )
          )
        //pl.push({'slug':'@t.slug','id':@t.id,'artist_id':@t.artistID,'price':@t.price,'title':'@escapeJavaScript(t.name)', 'file':'@(t.previewURL(domain))', 'duration':'@t.duration'});
      }
      Ok(generate(rs))
    }

  }
}
