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


object Forms {

  val trackUploadedForm = Form {
    tuple(
      "id" -> text,
      "session" -> text
    )

  }
  val idSessionForm = Form {
    tuple(
      "id" -> optional(text),
      "session" -> text
    )

  }

  val authTokenForm = Form {
    single("token" -> text)
  }
  val trackStatusForm = Form {
    single("ids" -> list(text))
  }
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

  val trackMapping = mapping(
    "id" -> longNumber,
    "artist_id" -> artist,
    "session" -> text,
    "file" -> optional(text),

    "name" -> text(minLength = 1, maxLength = 50),
    "slug" -> slug,
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
    "activate" -> boolean,
    "duration" -> number
  )(Track.apply)(Track.unapply)


  val albumForm: Form[(Album, Seq[Track])] = Form {
    tuple(
      "album" -> mapping(
        "id" -> longNumber,
        "artist_id" -> artist,
        "session" -> text,
        "name" -> text(minLength = 1, maxLength = 50),
        "artist" -> optional(text),
        "slug" -> slug,
        "active" -> boolean,
        "download" -> boolean,
        "donate" -> boolean,
        "price" -> of[Double],
        "art" -> optional(text),
        "about" -> optional(text),
        "credits" -> optional(text),
        "upc" -> optional(text),
        "releaseDate" -> optional(sqlDate("MM-dd-yyyy"))
      )(Album.apply)(Album.unapply),
      "tracks" -> seq(trackMapping)
    )
  }

  val singleTrackForm: Form[Track] = Form {
    single(
      "track" -> trackMapping
    )
  }

}
