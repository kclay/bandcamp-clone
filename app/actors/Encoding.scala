package actors


import akka.actor.Actor
import java.io.File
import models.Artist

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/28/12
 * Time: 9:27 AM
 */
//case class EncodingStatus(id: String, status: String)

case class EncodingStatus(ids:Array[String])
case class Encode(artist: Artist, id: String, file: File)

class Encoding extends Actor {
  protected def receive = {
    case Encode(artist, id, file) =>
  }
}
