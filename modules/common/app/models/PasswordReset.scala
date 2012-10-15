package models

import java.sql.Timestamp
import play.api.libs.Crypto
import org.squeryl.KeyedEntity

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 8/8/12
 * Time: 9:37 AM
 */

case class PasswordReset(artistID: Long) extends KeyedEntity[Long] {
  var id: Long = 0

  val token = Crypto.sign("%s-%s".format(artistID, System.nanoTime()))
  val createdAt = new Timestamp(System.currentTimeMillis())

  def withArtist = Artist.find(artistID)

  def this() = this(0)
}

object PasswordReset {

  import SiteDB._
  import org.squeryl.PrimitiveTypeMode._

  def find(token: String) = resets.where(r => r.token === token).headOption

  def delete(id: Long) = resets.delete(id)

  def apply(artist: Artist) = resets.insert(new PasswordReset(artist.id))
}
