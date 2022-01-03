import sbt.Keys._
import sbt._

object Common {
  val scala213 = "2.13.7"

  val settings = Seq(
    organization         := "com.streamingswap",
    organizationHomepage := Some(url("https://streamingswap.com")),
    scalaVersion         := scala213,
    excludeDependencies ++= Seq("org.slf4j" %% "slf4j-jdk14", "org.slf4j" %% "slf4j-log4j12"),
    Test / fork := true,
  )

  val playWsStandaloneVersion = "2.1.6"

  // Shared Dependencies
  val guice          = "com.google.inject"           % "guice"           % "5.0.1"
  val typesafeConfig = "com.typesafe"                % "config"          % "1.4.1"
  val scalaLogging   = "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.4"
  val logback        = "ch.qos.logback"              % "logback-classic" % "1.2.10"
  val json4sNative   = "org.json4s"                 %% "json4s-native"   % "4.0.2"

  // Testing
  val scalaTest        = "org.scalatest"            %% "scalatest"               % "3.2.9"                 % Test
  val scalaTestPlus    = "org.scalatestplus.play"   %% "scalatestplus-play"      % "5.1.0"                 % Test
  val scalaMock        = "org.scalamock"            %% "scalamock"               % "5.2.0"                 % Test
  val scalaCheck       = "org.scalacheck"           %% "scalacheck"              % "1.15.4"                % Test
  val playMockWS       = "de.leanovate.play-mockws" %% "play-mockws"             % "2.8.1"                 % Test
  val wsStandalone     = "com.typesafe.play"        %% "play-ahc-ws-standalone"  % playWsStandaloneVersion % Test
  val wsStandaloneJson = "com.typesafe.play"        %% "play-ws-standalone-json" % playWsStandaloneVersion % Test

}
