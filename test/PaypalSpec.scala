package test

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 7/25/12
 * Time: 5:05 PM 
 */

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._


class PaypalSpec extends Specification {

  import services.PayPal

  "Paypal" should {
    "return token" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val paypal = new PayPal()
        val token=paypal.express("First Test", 10.0, "http://google.com", "http://yahoo.com")
        //token must
      }
    }
  }
}
