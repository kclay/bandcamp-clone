import java.io.File
import sbt._

import Keys._
import PlayProject._


object ApplicationBuild extends Build {

  import Resolvers._

  val appName = "bulabowl"
  val appVersion = "1.0-SNAPSHOT"

  val dbDependencies = Seq(
    "org.squeryl" %% "squeryl" % "0.9.5-2" withSources(),
    //"org.scalaquery" % "scalaquery_2.9.0-1" % "0.9.5",
    "mysql" % "mysql-connector-java" % "5.1.18")
  val baseDependencies = Seq(
    // Add your project dependencies here,
    "org.mindrot" % "jbcrypt" % "0.3m",
    "jp.t2v" %% "play20.auth" % "0.3"
    // use single % instead of %% since binary are compatible
    //https://groups.google.com/group/scalaquery/browse_thread/thread/fbdd2f25dc4bf7d6

  ) ++ dbDependencies

  val appDependencies = Seq(

    "com.typesafe" %% "play-plugins-mailer" % "2.0.4"

  ) ++ baseDependencies





  val common = PlayProject(
    appName + "-common", appVersion, path = file("modules/common"), dependencies = baseDependencies
  ).settings(
    resolvers += tv2jp
  )


  val api = PlayProject(
    appName + "-api", appVersion, path = file("modules/api"), dependencies = baseDependencies
  ).dependsOn(common).settings(
    resolvers += tv2jp
  )


  object Resolvers {


    val tv2jp = "t2v.jp repo" at "http://www.t2v.jp/maven-repo/"

  }

  /*
  val adminArea = PlayProject(
    appName + "-admin", appVersion, path = file("modules/admin")
  ).dependsOn(common) */

  // Only compile the bootstrap bootstrap.less file and any other *.less file in the stylesheets directory
  def customLessEntryPoints(base: File): PathFinder = (
    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less") +++
      /*(base / "app" / "assets" / "stylesheets" / "bootstrap" * "responsive.less") +++*/
      (base / "app" / "assets" / "stylesheets" * "*.less")
    )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    // Add your own project settings here


    lessEntryPoints <<= baseDirectory(customLessEntryPoints),
    routesImport += "binders._",
    routesImport += "models._",
    templatesImport += "org.apache.commons.lang.StringEscapeUtils.escapeJavaScript"

  ).dependsOn(
    common, api
  )

}
