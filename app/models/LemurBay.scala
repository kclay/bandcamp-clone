package models

import play.api.cache._

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 9/17/12
 * Time: 12:23 PM 
 */


sealed abstract class GameScene {
  val name = ""
  val GENRES = Seq.empty[String]

  def genres: Seq[Long] = {
    import play.api.Play.current
    Cache.getOrElse("genres_" + name) {
      import models.SiteDB.{genres => _genres}
      import org.squeryl.PrimitiveTypeMode._

      from(_genres)(g =>
        where(g.name in GENRES)
          select (g.id)
      ).toSeq
    }
  }


  def unapply(value: String) = if (value.equals(name)) Some(this) else None
}

case object OldStationBar extends GameScene {
  override val name = "old-station-bar"
  override val GENRES = Seq("Country")


}

case object SpeckledGecko extends GameScene {
  override val name = "speckled-gecko"
  override val GENRES = Seq("Pop", "Dance")


}

case object RhythmBoat extends GameScene {
  override val name = "rhythm-boat"
  override val GENRES = Seq("Jazz", "Blues")
}

case object CoffeeShop extends GameScene {
  override val name = "coffee-shop"
  override val GENRES = Seq("Electronic", "Easy Listening")
}

