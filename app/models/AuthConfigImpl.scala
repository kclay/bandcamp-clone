package models

import play.api.mvc.Results._
import jp.t2v.lab.play20.auth._
import play.api.mvc._
import controllers.routes


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/4/12
 * Time: 12:10 PM
 */

trait AuthConfigImpl extends AuthConfig
{

  /**
   * A type that is used to identify a user.
   * `String`, `Int`, `Long` and so on.
   */
  type Id = Long

  /**
   * A type that represents a user in your application.
   * `User`, `Account` and so on.
   */
  type User = Account

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

  /**
   * A function that returns a `User` object from an `Id`.
   * Describe the procedure according to your application.
   */
  def resolveUser(id: Id): Option[User] = Accounts.findById(id)

  /**
   * A redirect target after a successful user login.
   */
  def loginSucceeded[A](request: Request[A]): PlainResult = Redirect(routes.Application.whitelabel)

  /**
   * A redirect target after a successful user logout.
   */
  def logoutSucceeded[A](request: Request[A]): PlainResult = Redirect(routes.Application.login)

  /**
   * A redirect target after a failed authentication.
   */
  def authenticationFailed[A](request: Request[A]): PlainResult = Redirect(routes.Application.login)

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
      case (Administrator, _) => true
      case (NormalUser, NormalUser) => true
      case _ => false
    }

}