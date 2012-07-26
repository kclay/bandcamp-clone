package models

import org.squeryl.PrimitiveTypeMode._
import scala.Some
import java.sql.Date
import org.squeryl.KeyedEntity

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 7/16/12
 * Time: 2:15 PM
 */


object Artist {
  private val SALT: String = "m^c*$kxz_qkwupq$by*fpi_czho=#8+k5dnakvd7x$gt#-&h+t";

  import SiteDB._

  def hasTag(id: Long): Boolean = inTransaction(artistTags.where(t => t.artistID === id).Count != 0)

  def domainAvail(domain: String): Boolean = artists.where(a => a.domain === domain).Count == 1


  def findByDomain(domain: String): Option[Artist] = inTransaction(artists.where(a => a.domain === domain).headOption)

  def authenticate(name: String, password: String): Option[Artist] = findByUsername(name).filter(a => a.pass == hash(password))

  def findByUsername(username: String): Option[Artist] = artists.where(a => a.username === username).headOption

  private def hash(pass: String): String = pass

  def findByEmail(email: String): Option[Artist] = artists.where(a => a.email === email).headOption

  def find(id: Long): Option[Artist] = artists.where(a => a.id === id).headOption



  def updateDomain(artistId: Long, domain: String) = inTransaction {
    update(artists)(a =>
      where(a.id === artistId)
        set(a.domain := domain, a.activated := true)
    )
  }


}

case class Artist(username: String, pass: String, email: String, name: String, domain: String = "", permission: String = "normal", activated: Boolean = false) extends KeyedEntity[Long] {
  var id: Long = 0
}


case class ArtistTag(artistID: Long, tagID: Long)

object ArtistTag {


  import SiteDB._

  def insert(artist: Artist, ts: List[String]) = inTransaction {


    val foundTags = Tag.find(ts)
    // insert tags that are already in db
    artistTags.insert(foundTags.map({
      tag => ArtistTag(artist.id, tag.id)
    }))
    // create flatten List[String]
    val flattenTags = foundTags.map(_.name)
    // find new tags that were not found in db
    val missingTags = ts.filter({
      tag => !flattenTags.contains(tag)
    })

    if (!missingTags.isEmpty) {
      // insert the new tags
      tags.insert(missingTags.map(Tag(_)))
      // search for the tags that were newly inserted
      artistTags.insert(Tag.find(missingTags).map({
        tag => ArtistTag(artist.id, tag.id)
      }))
    }


  }
}






