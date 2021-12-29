import sbt.Keys._
import sbt._

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / javacOptions := Seq(
  //format: off
  "-source", "11",
  "-target", "11",
  //format: on
)

Common.settings

maintainer := "tanner.mbt@gmail.com"

val runtimeDependencies = Seq(
  Common.typesafeConfig,
  Common.scalaLogging,
  Common.logback,
  Common.guice,              // eliminates the use of reflection
  play.sbt.PlayImport.guice, // needs both guice dependencies
  play.sbt.PlayImport.ws,
  Common.json4sNative,
)

val testDependencies = Seq(
  Common.scalaTest,
  Common.scalaTestPlus,
  Common.wsStandalone,
  Common.wsStandaloneJson,
)

libraryDependencies ++= runtimeDependencies ++ testDependencies

lazy val root = (project in file("."))
  .settings(
    name := "streaming_service_converter_3"
  )

Compile / scalaSource       := baseDirectory.value / "src/main/scala"
Compile / resourceDirectory := baseDirectory.value / "src/main/resources"

Test / scalaSource       := baseDirectory.value / "src/test/scala"
Test / resourceDirectory := baseDirectory.value / "src/test/resources"

enablePlugins(PlayScala, PlayService, JavaAppPackaging)

Compile / publishArtifact := false

// Commands
addCommandAlias("build", "prepare; test")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll")
addCommandAlias("check", "fmtCheck")
