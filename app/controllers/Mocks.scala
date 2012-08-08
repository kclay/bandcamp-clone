package controllers

import play.api.mvc.Controller
import jp.t2v.lab.play20.auth.Auth
import models.AuthConfigImpl
import services.PayPal
import play.api._

import play.api.mvc._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 7/25/12
 * Time: 5:14 PM
 */

object Mocks extends Controller with Auth with AuthConfigImpl with WithDB {

  import play.api.data._
  import play.api.data.Forms._

  def paypal() = Action {
    implicit request =>
      PayPal("a@yahoo.com", "First Test", 10.0, "http://" + request.host + "/mocks/paypal/callback", "http://yahoo.com").map(
        token => Redirect(PayPal.url(token))
      ).getOrElse(Ok("no_token"))

  }

  val callbackForm = Form(
    single(
      "token" -> text

    )
  )


}
