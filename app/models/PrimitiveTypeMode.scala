package models


import org.squeryl.{PrimitiveTypeMode => SquerylPrimitiveTypeMode}

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 6/21/12
 * Time: 1:17 PM
 */

object PrimitiveTypeMode extends SquerylPrimitiveTypeMode
{
  //implicit val permissionTEF = PrimitiveTypeSupport.stringTEF

  //implicit def permissionToTE(s: String) = permissionTEF.create(s)
}
