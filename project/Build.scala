import java.io.File
import sbt._

import Keys._
import PlayProject._


object ApplicationBuild extends Build {

  val appName = "bulabowl"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "jp.t2v" %% "play20.auth" % "0.3-SNAPSHOT",
    // use single % instead of %% since binary are compatible
    //https://groups.google.com/group/scalaquery/browse_thread/thread/fbdd2f25dc4bf7d6
    "org.squeryl" %% "squeryl" % "0.9.5-2" withSources(),
    //"org.scalaquery" % "scalaquery_2.9.0-1" % "0.9.5",
    "mysql" % "mysql-connector-java" % "5.1.18",
    "com.typesafe" %% "play-plugins-mailer" % "2.0.4"
    //"org.mindrot" % "jbcrypt" % "0.3m"
  )

  // Only compile the bootstrap bootstrap.less file and any other *.less file in the stylesheets directory
  def customLessEntryPoints(base: File): PathFinder = (
    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less") +++
      /*(base / "app" / "assets" / "stylesheets" / "bootstrap" * "responsive.less") +++*/
      (base / "app" / "assets" / "stylesheets" * "*.less")
    )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    // Add your own project settings here
    resolvers += "t2v.jp repo" at "http://www.t2v.jp/maven-repo/",
    resolvers += "Scala-Tools Maven2 Repository" at "http://scala-tools.org/repo-releases",
    lessEntryPoints <<= baseDirectory(customLessEntryPoints),
    routesImport += "binders.Binders",
    templatesImport += "org.apache.commons.lang.StringEscapeUtils.escapeJavaScript"

  )

}
