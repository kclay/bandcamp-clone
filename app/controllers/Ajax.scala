package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.libs.json._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/11/12
 * Time: 11:18 AM
 */

object Ajax extends Controller
{

  def tags(query: String) = Action {
    val found = Tags.search(query).map {
      case (t) => Map("name" -> t.name)
    }


    Ok(Json.toJson(found))
  }


}
