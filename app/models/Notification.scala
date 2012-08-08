package models

import com.typesafe.plugin._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 8/8/12
 * Time: 9:46 AM
 */

object Notification {

  import play.api.Play.current

  def apply(subject: String, email: String, content: String) = {
    val mail = use[MailerPlugin].email
    mail.setSubject(subject)

    mail.addRecipient(email)
    mail.addFrom("BulaBowl Notifications <notify@bulabowl.com>")
    //sends both text and html
    try {
      mail.send(content)
      true
    } catch {
      case _ => false
    }
  }
}
