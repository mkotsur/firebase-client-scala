name := "firebase-client-scala"
organization := "io.github.mkotsur"
// version := @see version.sbt

releasePublishArtifactsAction := PgpKeys.publishSigned.value

scalaVersion := "2.12.1"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

val circeVersion = "0.7.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "com.github.kxbmap" %% "configs" % "0.4.4"

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "org.bouncycastle" % "bcpkix-jdk15on" % "1.55"

libraryDependencies += "com.google.api-client" % "google-api-client" % "1.22.0" exclude("com.google.guava", "guava-jdk5")
libraryDependencies += "com.google.identitytoolkit" % "gitkitclient" % "1.2.7"


// Test dependencies
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"