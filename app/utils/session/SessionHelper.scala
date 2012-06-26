package utils.session

import play.api.mvc._
import models.Artist

import sun.misc.BASE64Encoder

object SessionHelper
{
  val sessionKey = "sessionId"
  val usernameKey = "username"

  def username(implicit request: RequestHeader) =
  {
    request.session.get(usernameKey).get
  }

  def authenticated(implicit request: RequestHeader) =
  {
    request.session.get(sessionKey).exists(_ => true)
  }

  def session(implicit request: RequestHeader) =
  {

    val session = request.cookies.get(Session.COOKIE_NAME).get.value
    new BASE64Encoder().encode(session.getBytes)


  }


}