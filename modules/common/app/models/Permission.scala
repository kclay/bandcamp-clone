package models

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/4/12
 * Time: 12:10 PM
 */


sealed trait Permission

case object Administrator extends Permission

case object NormalUser extends Permission