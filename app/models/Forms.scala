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
import models._


object Forms
{


  val signupFrom = Form {
    mapping(
      "username" -> text(minLength = 6)
        .verifying("Invalid username", {
        !Seq("admin", "guest").contains(_)
      }).
        verifying("This username is not available", {
        models.Artist.findByUsername(_).isEmpty
      }),
      "password" -> text(minLength = 6),
      "email" -> email.verifying("This email has already been registered", {
        models.Artist.findByEmail(_).isEmpty
      }
      ),
      "name" -> text(minLength = 4),
      "accept" -> checked("Please accept the terms and conditions")
    ) {
      (username, password, email, name, _) => Signup(username, password, email, name)
    } {
      s => Some(s.username, s.password, s.email, s.name, false)
    } /*.verifying(
      "This username is not avilable",
      (s: Signup) =>! Seq("admin", "guest").contains(s.username) && Artists$.findByName(s.username).isEmpty
    ).verifying("This email has already been registered",
      (s: Signup) => Artists$.findByEmail(s.email).isEmpty
    )  */

  }

  val loginForm = Form {
    mapping("username" -> text, "password" -> text)(Artist.authenticate)(_.map(u => (u.name, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }
  val domainForm = Form(
    single(
      "domain" -> text(minLength = 4, maxLength = 25)
    ) verifying(
      "Domain already taken", result => result match {
      case (domain) => Artist.findByDomain(domain).isEmpty
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
        "date" -> optional(sqlDate("MM-dd-yyyy")),
        "activate" -> boolean
      )(Track.apply)(Track.unapply)
    )
  }

}