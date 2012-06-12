package controllers

import jp.t2v.lab.play20.auth.{AuthConfig, LoginLogout}
import utils.SessionHelper
import annotation.tailrec
import util.Random
import java.security.SecureRandom
import play.api.mvc.{Session, Controller, PlainResult, Request}
import models.Artist

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/11/12
 * Time: 7:28 PM
 */

trait MyLoginLogout extends LoginLogout
{
  self: Controller with AuthConfig =>
  override def gotoLoginSucceeded[A](userId: Id)(implicit request: Request[A]): PlainResult =
  {
    resolver.removeByUserId(userId)
    val sessionId = generateSessionId2(request)
    var session = resolver.store(sessionId, userId, sessionTimeoutInSeconds)
    val artist = resolveUser(userId).get.asInstanceOf[Artist]

    session = Map("sessionId" -> sessionId, SessionHelper.usernameKey -> artist.name).foldLeft[Session](session) {
      _ + _
    }



    loginSucceeded(request).withSession(session)
  }


  @tailrec
  private def generateSessionId2[A](implicit request: Request[A]): String =
  {
    val table = "abcdefghijklmnopqrstuvwxyz1234567890-_.!~*'()"
    val token = Stream.continually(random2.nextInt(table.size)).map(table).take(64).mkString
    if (resolver.exists(token)) generateSessionId2(request) else token
  }

  private val random2 = new Random(new SecureRandom())
}
