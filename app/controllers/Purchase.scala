package controllers

import actions.SquerylTransaction
import play.api._
import libs.Crypto
import play.api.mvc._
import models._
import services._
import models.Forms.{purchaseForm, paypalCallbackForm}
import scala.Some
import views._
import models.Download
import scala.Some
import com.codahale.jerkson.Json._

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
          val (artistID, price) = value

          withToken(artistID, price) {
            Album.bySlug(artistID, album)
          }


        }
      )


  }

  private def withToken(artistID: Long, price: Double)(block: => Option[SaleAbleItem])(implicit request: RequestHeader) = {

    def e(message: String) = BadRequest(generate(Map("error" -> message)))
    block.map {
      item => withSig(artistID, price, item).map {
        sig: String => withPaypal(sig, item, price, "http://bulabowl.com") match {
          case Left(error) => e(error.message)
          case Right(token) => Transaction(sig, item, price, token.key)
          Ok(generate(Map("token" -> token.key)))
        }
      }.getOrElse(e("Unable to checkout."))
    }.getOrElse(e("Unable to find item."))
  }

  private def withSig(artistId: Long, price: Double, item: SaleAbleItem)(implicit request: RequestHeader) =
    Some(Crypto.sign("%s-%s-%s-%s-%d".format(request.remoteAddress,
      String.valueOf(artistId), String.valueOf(price), item.signature, System.nanoTime())))

  def checkout(token: String) = TransAction {
    Action {
      Transaction.status(token, Transaction.STATUS_CHECKOUT)
      Redirect(PayPal.url(token))
    }
  }

  private def withPaypal(sig: String, item: SaleAbleItem, price: Double, cancelURL: String)(implicit request: RequestHeader) = {

    val artist = item.artist.get
    PayPal(artist.email, item.itemTitle, price, routes.Purchase.callback(sig).absoluteURL(), cancelURL)
  }


  def withCommit(details: Map[String, String]) = {
    val commit = PayPal.commit(details)
    for {
      ok <- PayPal ok commit


    } yield commit
  }

  def withEmail(details: Map[String, String], commit: Map[String, String], token: String)(implicit request: RequestHeader) = {

    Transaction.commit(token,
      commit get (PayPal.FIELD_CORRELATIONID),
      commit get (PayPal.FIELD_TRANSACTIONID)
    )

    var email = details.get(PayPal.FIELD_EMAIL).get
    if (email.contains("conceptual-ideas.com"))
      email = "info@ihaveinternet.com";

    Transaction.withArtistAndItem(token).map {
      case (trans: Transaction, artist: Artist, item: SaleAbleItem) =>
        import play.api.Play.current
        import com.typesafe.plugin._
        val download = new Download(token, item.signature, item.itemType)
        val htmlContent = html.email.downloadHtml(artist.name, item.itemTitle, download.signedURL).body
        val textContent = html.email.downloadText(artist.name, item.itemTitle, download.signedURL).body
        val mail = use[MailerPlugin].email
        mail.setSubject("Your download from %s".format(artist.name))
        mail.addRecipient(email)
        mail.addFrom("%s <noreply@%s>".format(artist.name, request.host))

        //sends both text and html
        try {
          mail.send(textContent, htmlContent)
        } catch {
          case _ =>
        }

      case _ =>
    }
  }

  def callback(sig: String) = TransAction {
    implicit request =>
      Transaction.bySig(sig).map {

        trans => Ok(html.purchase(sig))


      }.getOrElse(BadRequest("error"))
  }

  def ajaxCommit(sig: String) = TransAction {
    implicit request =>
      Transaction.bySig(sig).map {
        trans =>
          val token = trans.token
          val details = PayPal details (token)

          Transaction.status(token, Transaction.STATUS_CALLBACK)

          (for {
            ok <- PayPal ok details
            commit <- withCommit(details)
            s <- Sale(trans)
            _ <- withEmail(details, commit, token)
          } yield Ok("ok")
            ).getOrElse(Ok("error"))
      }.getOrElse(Ok("error"))
  }


  def track(track: String) = TransAction {
    implicit request =>
      purchaseForm.bindFromRequest.fold(
        errors => BadRequest("error"),
        value => {
          val (artistID, price) = value
          withToken(artistID, price) {
            Track.bySlug(artistID, track)
          }

        }
      )
  }

}
