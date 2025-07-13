ThisBuild / version := sys.env.getOrElse("PROJECT_VERSION", "0.0.0-LOCAL-SNAPSHOT")
ThisBuild / scalaVersion := "3.7.1"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

val catsVersion = "2.13.0"
val catsEffectVersion = "3.6.2"
val fs2Version = "3.12.0"
val http4sVersion = "0.23.30"
val circeVersion = "0.14.14"
val doobieVersion = "1.0.0-RC9"
val pureConfigVersion = "0.17.9"
val logbackVersion = "1.5.18"
val logstashLogbackEncoderVersion = "8.1"
val log4catsVersion = "2.7.1"
val munitVersion = "1.1.1"
val munitCatsEffectVersion = "1.0.7"
val tapirVersion = "1.11.35"
val otel4sVersion = "0.13.1"
val openTelemetryVersion = "1.51.0"
val openTelemetryInstrumentationVersion = "2.17.1"

lazy val openTelemetryBom = com.here.bom.Bom(
  "io.opentelemetry" % "opentelemetry-bom-alpha" % s"$openTelemetryVersion-alpha"
)
lazy val openTelemetryInstrumentationBomAlpha = com.here.bom.Bom(
  "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-bom-alpha" % s"$openTelemetryInstrumentationVersion-alpha"
)

lazy val root = (project in file("."))
  .settings(
    openTelemetryBom,
    openTelemetryInstrumentationBomAlpha
  )
  .settings(
    name := "todo-backend",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion % Runtime,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % openTelemetryBom.key.value % Runtime,
      "io.opentelemetry.instrumentation" % "opentelemetry-logback-appender-1.0" % openTelemetryInstrumentationBomAlpha.key.value % Runtime,
      "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry-java17" % openTelemetryInstrumentationBomAlpha.key.value,
      "net.logstash.logback" % "logstash-logback-encoder" % logstashLogbackEncoderVersion % Runtime,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
      "org.typelevel" %% "munit-cats-effect-3" % munitCatsEffectVersion % Test,
      "org.typelevel" %% "otel4s-oteljava" % otel4sVersion
    ),
    dependencyOverrides ++= openTelemetryInstrumentationBomAlpha.key.value.bomDependencies ++
      openTelemetryBom.key.value.bomDependencies,
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
    ),
    jibBaseImage := "eclipse-temurin:21-jre",
    jibRegistry := sys.env.getOrElse("DOCKER_REGISTRY", "missing-DOCKER_REGISTRY-env-var"),
    jibOrganization := sys.env.getOrElse("DOCKER_REPOSITORY", "missing-DOCKER_REPOSITORY-env-var")
  )
