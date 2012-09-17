package models

import play.api.data._


/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/20/12
 * Time: 3:48 PM
 */

import play.api.data.Forms._

import utils.format.Formats._


object Forms {


  val forgotForm = Form(
    single("email" -> artistEmail)
  )
  var itemTagsMapping = tuple(
    "slug" -> text,
    "tags" -> list(text)
  )

  val saveTagsForm = Form(
    tuple(
      "kind" -> text,
      "items" -> list(itemTagsMapping)
    )
  )
  val resetForm = Form(
    tuple(
      "password" -> text,
      "confirm_password" -> text
    ) verifying(
      // Add an additional constraint: both passwords must match
      "Passwords don't match", passwords => passwords._1 == passwords._2
      )
  )
  val downloadForm = Form(

    mapping(
      "token" -> text,
      "item" -> text,


      "kind" -> text,
      "from" -> text,
      "sig" -> text
    )(Download.apply)(Download.unapply)


  )
  val purchaseForm = Form {
    tuple("artist_id" -> longNumber, "price" -> of[Double])
  }
  val paypalCallbackForm = Form(
    single(
      "token" -> text

    )
  )

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
        models.Artist.byEmail(_).isEmpty
      }
      ),
      "name" -> text(minLength = 4)
      /*,
      "code" -> text.verifying("Invalid Signup Code", {
        models.PromoCode.find(_).isDefined
      }

    )*/ ,
      "accept" -> checked("Please accept the terms and conditions")
    ) {
      (username, password, email, name, _) => Signup(username, password, email, name)
    } {
      s => Some(s.username, s.password, s.email, s.name, false)
    } /*.verifying(
      "This username is not avilable",
      (s: Signup) =>! Seq("admin", "guest").contains(s.username) && Artists$.findByName(s.username).isEmpty
    ).verifying("This email has already been registered",
      (s: Signup) => Artists$.byEmail(s.email).isEmpty
    )  */

  }

  val loginForm = Form {
    mapping("username" -> text, "password" -> text)(Artist.authenticate)(_.map(u => (u.name, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }
  val domainForm = Form(
    single(
      "domain" -> slug
    ) verifying(
      "Domain already taken", result => result match {
      case (domain) => Artist.findByDomain(domain).isEmpty
    })
  )

  val tagsForm = Form {
    tuple(
      "genre" -> longNumber,
      "tags" -> optional(text),
      "location" -> optional(text)
    )
  }

  val trackMapping = mapping(
    "id" -> longNumber,
    "artist_id" -> artist,
    "session" -> text,
    "file" -> optional(text),
    "fileName" -> optional(text),
    "name" -> text(minLength = 1, maxLength = 50),
    "slug" -> slug,
    "donateMore" -> boolean,
    "download" -> boolean,
    "price" -> of[Double],

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
        "donateMore" -> boolean,
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
