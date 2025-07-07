ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.1"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

val catsVersion = "2.13.0"
val catsEffectVersion = "3.6.1"
val fs2Version = "3.12.0"
val http4sVersion = "0.23.30"
val circeVersion = "0.14.14"
val doobieVersion = "1.0.0-RC9"
val pureConfigVersion = "0.17.9"
val logbackVersion = "1.5.18"
val log4catsVersion = "2.7.1"
val munitVersion = "1.1.1"
val munitCatsEffectVersion = "1.0.7"
val tapirVersion = "1.11.35"

lazy val root = (project in file("."))
  .settings(
    name := "todo-backend-scala3-cats",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion % Runtime,
      "org.scalameta" %% "munit" % munitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % munitCatsEffectVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    scalacOptions ++= Seq(
      "-Wnonunit-statement",
      "-Wsafe-init",
      "-Wunused:all",
      "-Wvalue-discard",
      "-deprecation",
      "-feature",
      // "-indent",
      // "-new-syntax",
      // "-rewrite",
      "-source:3.7-migration",
      "-unchecked"
    )
  )
