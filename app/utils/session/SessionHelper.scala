package utils.session

import play.api.mvc._
import models.Artist
import com.ning.http.util.Base64


;

//import sun.misc.BASE64Encoder

object SessionHelper {
  val sessionKey = "sessionId"
  val usernameKey = "username"
  val artistKey = "artist"

  def username(implicit request: RequestHeader) = {
    request.session.get(usernameKey).get
  }

  def band(implicit request: RequestHeader) = {
    username(request)
  }

  def authenticated(implicit request: RequestHeader) = {
    request.session.get(sessionKey).exists(_ => true)
  }



  def artist = {

    val a = utils.Utils.artist.get
    a
  }

  def session(implicit request: RequestHeader) = {

    val session = request.cookies.get(Session.COOKIE_NAME).get.value
    Base64.encode(session.getBytes)



    //new BASE64Encoder().encode(session.getBytes).replaceAll("[\r|\n]","")


  }


}