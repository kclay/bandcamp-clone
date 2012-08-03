package controllers

import actions.SquerylTransaction
import play.api._
import play.api.mvc._
import models._
import services.PayPal
import models.Forms.{purchaseForm, paypalCallbackForm}
import scala.Some
import views._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 8/2/12
 * Time: 5:23 PM
 */

object Purchase extends Controller with SquerylTransaction {

  def album(album: String) = TransAction {
    implicit request =>

      purchaseForm.bindFromRequest.fold(
        errors => BadRequest("error"),
        value => {
          val (artistId, price) = value
          Album.bySlug(artistId, album).map {
            a =>
              withPaypal(a, price, "http://bulabown.com").map({
                token =>
                  Transaction(a, price, token)
                  Ok(token)
              }).getOrElse(BadRequest("error"))
          }.getOrElse(BadRequest("error"))
        }
      )


  }

  def checkout(token: String) = TransAction {
    Action {
      Transaction.status(token, Transaction.STATUS_CHECKOUT)
      Redirect(PayPal.url(token))
    }
  }

  private def withPaypal(item: SaleAbleItem, price: Double, cancelURL: String)(implicit request: RequestHeader) = {

    PayPal(item.itemTitle, price, routes.Purchase.callback.absoluteURL(), cancelURL).map(
      token => Some(token)
    ).getOrElse(None)
  }

  def callback() = TransAction {
    implicit request =>
      paypalCallbackForm.bindFromRequest.fold(
        errors => BadRequest("wtf"),
        token => {
          val details = PayPal details (token)
          Transaction.status(token, Transaction.STATUS_CALLBACK)
          if (PayPal ok details) {
            val email = "info@ihaveinternet.com" //details.get(PayPal.FIELD_EMAIL).get
            val commit = PayPal.commit(details)
            //if (PayPal ok commit) {
            Transaction.commit(token,
              commit get (PayPal.FIELD_CORRELATIONID),
              commit get (PayPal.FIELD_TRANSACTIONID)
            )
            Transaction.withArtistAndItem(token).map {
              case (trans: Transaction, artist: Artist, item: SaleAbleItem) =>
                import play.api.Play.current
                import com.typesafe.plugin._
                val download = new Download(token, item.signature, item.itemType)
                val htmlContent = html.email.downloadHtml(artist.name, item.itemTitle, download.url).body
                val textContent = html.email.downloadText(artist.name, item.itemTitle, download.url).body
                val mail = use[MailerPlugin].email
                mail.setSubject("Your download from %s".format(artist.name))
                mail.addRecipient(email)
                mail.addFrom("%s <noreply@%s>".format(artist.name, request.host.split(":")(0)))

                //sends both text and html
                mail.send(textContent, htmlContent)
              case _ =>
            }
            Ok("Details : %s\n\n Commit : %s\n".format(details.mkString("\n"), commit.mkString("\n")))

            // http :// meekmill.bandcamp.com / download ? enc = any & from = email & id = 1329670172 & payment_id = 559531198 & sig = 68d 2746055 ab8bca2df60e14d793c494 & type = album
            /* } else {
             BadRequest("error")
           } */
          } else {
            BadRequest("error_no_details")
          }
        }

      )
  }


  def track(track: String) = TransAction {
    implicit request =>
      purchaseForm.bindFromRequest.fold(
        errors => BadRequest("error"),
        value => {
          val (artistId, price) = value
          Track.bySlug(artistId, track).map {
            i =>
              withPaypal(i, price, "http://bulabown.com").map({
                token =>
                  Transaction(i, price, token)
                  Ok(token)
              }).getOrElse(BadRequest("error"))
          }.getOrElse(BadRequest("error"))
        }
      )
  }

}
