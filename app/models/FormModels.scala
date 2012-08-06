package models

import play.api.mvc.RequestHeader

import play.api.libs.Crypto

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/20/12
 * Time: 9:17 PM
 */
case class Signup(username: String, password: String, email: String, name: String)

case class AlbumModel(album: Album)

case class Download(token: String, item: String, kind: String, from: String = "email", sig: String = "") {


  lazy val withItem = Transaction.withItem(token).get

  def query = "from=%s&token=%s&item=%s&kind=%s".format(from, token, item, kind)

  def url(implicit request: RequestHeader) = "http://%s/download?%s".format(
    request.host, query)

  def signedURL(implicit request: RequestHeader) = {
    val u = url(request)
    u + "&sig=" + Crypto.sign(query)
  }


  def valid(implicit request: RequestHeader) = {
    val u = url(request)
    val maybeSig = Crypto.sign(query)
    maybeSig.equals(sig)
  }
}