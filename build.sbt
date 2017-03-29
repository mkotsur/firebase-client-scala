name := "firebase-rest-client-scala"

version := "1.0"

scalaVersion := "2.12.1"

val circeVersion = "0.7.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "com.github.kxbmap" %% "configs" % "0.4.4"

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "com.pauldijou" %% "jwt-core" % "0.12.0"

libraryDependencies += "com.google.api-client" % "google-api-client" % "1.22.0" exclude("com.google.guava", "guava-jdk5")
libraryDependencies += "com.google.identitytoolkit" % "gitkitclient" % "1.2.7"


// Test dependencies
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"