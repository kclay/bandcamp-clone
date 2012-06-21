package models

import play.api.data._
import play.api.data.Forms._


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/20/12
 * Time: 3:48 PM
 */

import play.api.data.Forms._

import utils.format.Formats._


object Forms
{


  val domainForm = Form(
    single(
      "domain" -> text(minLength = 4, maxLength = 25)
    ) verifying(
      "Domain already taken", result => result match {
      case (domain) => models.Artists.findByDomain(domain).isEmpty
    })
  )

  val tagsForm = Form {
    tuple(
      "genre" -> number,
      "tags" -> optional(text),
      "location" -> optional(text)
    )
  }
  val singleTrackForm: Form[Track] = Form {
    single(
      "track" -> mapping(
        "id" -> longNumber,
        "artist_id" -> longNumber,
        "name" -> text(minLength = 1, maxLength = 50),
        "donate" -> boolean,
        "download" -> boolean,
        "price" -> of[Double],
        "license" -> text,
        "artist" -> optional(text),
        "art" -> optional(text),
        "lyrics" -> optional(text),
        "about" -> optional(text),
        "credits" -> optional(text),
        "date" -> optional(sqlDate("MM-dd-yyyy"))
      )(Track.apply)(Track.unapply)
    )
  }

}
