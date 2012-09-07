package models

import play.api.mvc.Results._
import jp.t2v.lab.play20.auth._
import play.api.mvc._
import controllers.routes
import utils.session.SessionHelper

import play.api.cache.Cache


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/4/12
 * Time: 12:10 PM
 */

trait AuthConfigImpl extends AuthConfig {

  /**
   * A type that is used to identify a user.
   * `String`, `Int`, `Long` and so on.
   */
  type Id = Long

  /**
   * A type that represents a user in your application.
   * `User`, `Artist` and so on.
   */
  type User = Artist

  /**
   * A type that is defined by every action for authorization.
   * This sample uses the following trait.
   *
   * sealed trait Permission
   * case object Administrator extends Permission
   * case object NormalUser extends Permission
   */
  type Authority = Permission

  /**
   * A `ClassManifest` is used to get an id from the Cache API.
   * Basically use the same setting as the following.
   */
  val idManifest: ClassManifest[Id] = classManifest[Id]

  /**
   * A duration of the session timeout in seconds
   */
  val sessionTimeoutInSeconds: Int = 3600
  // 2 hours
  val userCacheTimeoutInSeconds: Int = 60 * 60 * 2;


  /**
   * A function that returns a `User` object from an `Id`.
   * Describe the procedure according to your application.
   */
  def resolveUser(id: Id): Option[User] = {
    import org.squeryl.PrimitiveTypeMode._
    import utils.Utils.{artist => setArtist}

    val artist = inTransaction(Artist.find(id))
    setArtist(artist)
    artist
  }

  /**
   * A redirect target after a successful user login.
   */
  def loginSucceeded[A](request: Request[A]): PlainResult = {
    val uri = request.session.get("access_uri").getOrElse(routes.Artists.index.url)
    request.session - "access_uri"
    Redirect(uri)

  }


  /**
   * A redirect target after a successful user logout.
   */
  def logoutSucceeded[A](request: Request[A]): PlainResult = Redirect(routes.Application.login).withNewSession

  /**
   * A redirect target after a failed authentication.
   */
  def authenticationFailed[A](request: Request[A]): PlainResult =
    Redirect(routes.Application.login).withSession("access_uri" -> request.uri)

  /**
   * A redirect target after a failed authorization.
   */
  def authorizationFailed[A](request: Request[A]): PlainResult = Forbidden("no permission")

  /**
   * A function that authorizes a user by `Authority`.
   * Describe the procedure according to your application.
   */
  def authorize(user: User, authority: Authority): Boolean =
    (user.permission, authority) match {
      case ("admin", _) => true
      case ("normal", NormalUser) => true
      case _ => false
    }

  override def resolver[A](implicit request: Request[A]): RelationResolver[Id] = new CookieRelationResolver[Id, A](request)

}