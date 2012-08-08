package services

import play.api.libs.ws.WS
import models.{Track, Album}
import utils.Utils.urldecode
import play.api.Play
import com.ning.http.client.AsyncHttpClientConfig
import java.text.DecimalFormat
import java.net.InetAddress
import java.util.concurrent.TimeUnit

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 7/25/12
 * Time: 4:48 PM
 */

case class Nvp() {
  var data = Map.empty[String, Seq[String]]

  def put(key: String, value: Any) = {
    data += key -> Seq(value.toString)
    this
  }
}

trait PayPalClient {

  import play.api.Play.current

  import java.util.concurrent.TimeUnit

  val FIELD_EMAIL = "EMAIL"
  val FIELD_CORRELATIONID = "CORRELATIONID"
  val FIELD_TRANSACTIONID = "TRANSACTIONID"
  val decimalFormat = new DecimalFormat("##.00")
  lazy val app = Play.application
  lazy val config = app.configuration

  lazy val password = config.getString("paypal.password").get
  lazy val username = config.getString("paypal.username").get
  lazy val signature = config.getString("paypal.signature").get
  lazy val env = config.getString("paypal.env").get
  lazy val appID = config.getString("paypal." + env + ".app").get
  lazy val timeout = java.lang.Long.parseLong(config.getString("paypal.timeout").get)

  lazy val api = "https://api-3t.%s.paypal.com/nvp".format(env)

  lazy val site = if (env == "sandbox") "https://www.sandbox.paypal.com/cgi-bin/webscr?" else "https://www.paypal.com/cgi-bin/webscr?"

  def checkoutWithEmail(email: String, title: String, amount: Double, returnURL: String, cancelURL: String, currencyCode: String = "USD"): Option[String] = None

  def ok(results: Map[String, String]) = results.get("ACK").map {
    a => if (a.equals("Success")) Some(true) else None
  }

  def url(token: String) = site + "cmd=_express-checkout&token=" + token

  def client = WS.url(api)

  protected def defaultParams(method: String) = {
    val params = new Nvp()





    // Add login infos
    params.put("METHOD", method);
    params.put("VERSION", "62.0");
    params.put("USER", username);
    params.put("PWD", password);
    params.put("SIGNATURE", signature);
    params
  }

  def checkout(title: String, amount: Double, returnURL: String, cancelURL: String, currencyCode: String = "USD") = {
    val params = defaultParams("SetExpressCheckout")

    // Add login infos
    params.put("METHOD", "SetExpressCheckout");
    params.put("VERSION", "62.0");
    params.put("USER", username);
    params.put("PWD", password);
    params.put("SIGNATURE", signature);

    //

    params.put("RETURNURL", returnURL);
    params.put("CANCELURL", cancelURL);
    params.put("LOCALECODE", "US");
    params.put("NOSHIPPING", "1"); // No shipping address
    // params.put("SOLUTIONTYPE", "Sole"); // No paypal account needed
    // params.put("LANDINGPAGE", "Billing"); // Non login page
    // params.put("CHANNELTYPE", "Merchant");
    params.put("PAYMENTREQUEST_0_AMT", decimalFormat.format(amount))

    params.put("L_PAYMENTREQUEST_0_NAME0", title)
    params.put("L_PAYMENTREQUEST_0_AMT0", decimalFormat.format(amount))
    params.put("L_PAYMENTREQUEST_0_DESC0", title)
    params.put("PAYMENTREQUEST_0_PAYMENTACTION", "Sale")
    //params.put("PAYMENTACTION", paymentType);
    params.put("CURRENCYCODE", currencyCode);
    params.put("PAYMENTREQUEST_0_CUSTOM", title)



    val results = call(params)
    results.get("TOKEN")
  }

  def amount(token: String) = {

    val value = details(token).get("PAYMENTREQUEST_0_AMT").getOrElse("0")
    java.lang.Double.parseDouble(value)
  }

  def details(token: String) = {
    val params = defaultParams("GetExpressCheckoutDetails")
    params.put("TOKEN", token)
    call(params)
  }

  def commit(results: Map[String, String]) = {
    call(defaultParams("DoExpressCheckoutPayment")
      .put("TOKEN", results get "TOKEN" get)
      .put("AMT", results get "AMT" get)
      .put("PAYERID", results get "PAYERID" get)
      .put("PAYMENTACTION", "Sale"))

  }


  def withResponse(response: String) = response.split("&").map(_.split("=")).map(p => (p(0) -> decode(p.drop(1).mkString("")))).toMap

  def call(params: Nvp) = {
    // Call paypal

    val result = client.post(params.data)
    val response = result.await(timeout, TimeUnit.SECONDS).get.body

    // Parse result
    withResponse(response)


  }

  private def decode(data: String) = java.net.URLDecoder.decode(data, "UTF-8")
}

case class PaypalExpress() extends PayPalClient {

}

case class PaypalAdaptive() extends PayPalClient {

  override val FIELD_EMAIL = "sender.email"
  override val FIELD_CORRELATIONID = "responseEnvelope.correlationId"
  override val FIELD_TRANSACTIONID = "paymentInfoList.paymentInfo(0).transactionId"
  lazy val tld = if (env == "sandbox") "sandbox.paypal.com" else "paypal.com"
  override lazy val api = "https://svcs.%s/AdaptivePayments/".format(tld)
  lazy val account = config.getString("paypal.account").get
  lazy val percentage = java.lang.Double.parseDouble(config.getString("paypal.percentage").get)

  override lazy val site = "https://www.%s/cgi-bin/webscr?cmd=_ap-payment&paykey=".format(tld)
  //override lazy val site = "https://www.%s/webapps/adaptivepayment/flow/pay?paykey=".format(tld)

  override def url(token: String) = site + token


  override def details(token: String) = {


    call("PaymentDetails", defaultParams.put("payKey", token))
  }

  protected def defaultParams = {
    new Nvp()
      .put("requestEnvelope.errorLanguage", "en_US")
      .put("requestEnvelope.detailLevel", "ReturnAll")

  }


  override def commit(results: Map[String, String]) = call("ExecutePayment", defaultParams.put("payKey", results.get("payKey").get))

  override def ok(results: Map[String, String]) = results.get("responseEnvelope.ack").map {
    a => if (a.equals("Success")) Some(true) else None
  }

  override def checkoutWithEmail(email: String, title: String, amount: Double, returnURL: String, cancelURL: String, currencyCode: String = "USD"): Option[String] = {
    val cut = (amount * percentage)
    val params = defaultParams
      .put("actionType", "CREATE")
      .put("receiverList.receiver(0).email", email)
      .put("receiverList.receiver(0).amount", decimalFormat.format(amount))
      .put("receiverList.receiver(0).primary", "true")
      // .put("receiverList.receiver(0).paymentType", "DIGITALGOODS")

      .put("feesPayer", "PRIMARYRECEIVER")
      //.put("senderEmail", account)
      .put("currencyCode", "USD")
      .put("cancelUrl", cancelURL)
      .put("returnUrl", returnURL)
      .put("receiverList.receiver(1).email", account)
      .put("receiverList.receiver(1).amount", decimalFormat.format(cut))

    // .put("receiverList.receiver(1).paymentType", "DIGITALGOODS")


    val results = call("Pay", params)


    results.get("payKey")
  }

  override def amount(token: String) = {

    val value = details(token).get("PAYMENTREQUEST_0_AMT").getOrElse("0")
    java.lang.Double.parseDouble(value)
  }

  def call(method: String, params: Nvp) = {
    // Call paypal


    val result = WS.url(api + method).
      withHeaders(("X-PAYPAL-SECURITY-USERID", username),
      ("X-PAYPAL-SECURITY-PASSWORD", password),
      ("X-PAYPAL-SECURITY-SIGNATURE", signature),
      ("X-PAYPAL-SECURITY-IPADDRESS", InetAddress.getLocalHost.getHostAddress),
      ("X-PAYPAL-REQUEST-DATA-FORMAT", "NV"),
      ("X-PAYPAL-RESPONSE-DATA-FORMAT", "NV"),
      ("X-PAYPAL-APPLICATION-ID", appID))
      .post(params.data)
    val response = result.await(timeout, TimeUnit.SECONDS).get.body

    // Parse result
    withResponse(response)


  }

}

object PayPal {

  import play.api.Play.current

  val FIELD_EMAIL = service.FIELD_EMAIL
  val FIELD_CORRELATIONID = service.FIELD_CORRELATIONID
  val FIELD_TRANSACTIONID = service.FIELD_TRANSACTIONID

  lazy val app = Play.application
  lazy val config = app.configuration
  lazy val env = config.getString("paypal.env").get

  lazy val service = new PaypalAdaptive()

  def url(token: String): String = service.url(token)

  def ok(results: Map[String, String]) = service.ok(results)

  def commit(results: Map[String, String]) = service.commit(results)

  def details(token: String) = {
    service.details(token)
  }

  def amount(token: String) = {
    service.amount(token)
  }

  def apply(email: String, title: String, amount: Double, returnURL: String, cancelURL: String, currencyCode: String = "USD") = {
    service.checkoutWithEmail(email, title, amount, returnURL, cancelURL, currencyCode)
  }


}
