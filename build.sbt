name := """botslandia"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )

libraryDependencies += "org.squeryl" % "squeryl_2.11" % "0.9.5-7"

libraryDependencies += "postgresql" % "postgresql" % "9.1-901-1.jdbc4"

libraryDependencies += "com.restfb" % "restfb" % "1.6.11"

libraryDependencies += "com.h2database" % "h2" % "1.3.167"


// Web Jars

// Delyan: had to pin this to "2.3.0-3";  with 2.5.0 it breaks:
//    [error] /Users/de/src/botslandia/conf/routes:17: object WebJarAssets is not a member of package controllers
libraryDependencies += "org.webjars" %% "webjars-play" % "2.3.0-3"

libraryDependencies += "org.webjars" % "npm" % "2.14.14"

libraryDependencies += "org.webjars.npm" % "react" % "0.14.8"

libraryDependencies += "org.webjars.bower" % "dropzone" % "4.3.0"

libraryDependencies += "org.webjars" % "bootstrap" % "3.3.1"

libraryDependencies += "com.dropbox.core" % "dropbox-core-sdk" % "2.0.1"

// play.Project.playScalaSettings
