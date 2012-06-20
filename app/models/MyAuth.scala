package models

import jp.t2v.lab.play20.auth.{AuthConfig, Auth}
import play.api.mvc._
import sun.misc.BASE64Decoder

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/19/12
 * Time: 3:42 PM
 */

/*class MyAuth extends Auth
{
  self: Controller with AuthConfig =>

  def authorizedAction(authority: Authority)(f: User => Request[AnyContent] => Result): Action[AnyContent] =
    authorizedAction(BodyParsers.parse.anyContent, authority)(f)

  def authorizedAction[A](p: BodyParser[A], authority: Authority)(f: User => Request[A] => Result): Action[A] =
    Action(p)(req => authorized(authority)(req).right.map(u => f(u)(req)).merge)

  def optionalUserAction(f: Option[User] => Request[AnyContent] => Result): Action[AnyContent] =
    optionalUserAction(BodyParsers.parse.anyContent)(f)

  def optionalUserAction[A](p: BodyParser[A])(f: Option[User] => Request[A] => Result): Action[A] =
    Action(p)(req => f(restoreUser(req))(req))

  def authorized[A](authority: Authority)(implicit request: Request[A]): Either[PlainResult, User] = for {
    user <- restoreUser(request).toRight(authenticationFailed(request)).right
    _ <- Either.cond(authorize(user, authority), (), authorizationFailed(request)).right
  } yield user


  private def restoreUser[A](implicit request: Request[A]): Option[User] =
  {
    val sessionId =

      for {
        sessionId <- request.queryString.get("token")
          .map(t => {
          val session = Session.decode(new BASE64Decoder().decodeBuffer(t.head).map(_.toChar).mkString)
          session.get("sessionId")

        }.getOrElse(request.session.get("sessionId")))
        userId <- resolver.sessionId2userId(sessionId)
        user <- resolveUser(userId)
      } yield {
        resolver.prolongTimeout(sessionId, sessionTimeoutInSeconds)
        user
      }
  }

}      */
