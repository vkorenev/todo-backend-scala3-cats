ThisBuild / version := sys.env.getOrElse("PROJECT_VERSION", "0.0.0-LOCAL-SNAPSHOT")
ThisBuild / scalaVersion := "3.7.1"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

val catsVersion = "2.13.0"
val catsEffectVersion = "3.6.2"
val fs2Version = "3.12.0"
val http4sVersion = "0.23.30"
val http4sOtel4sMiddlewareVersion = "0.13.0"
val otel4sVersion = "0.13.1"
val circeVersion = "0.14.14"
val doobieVersion = "1.0.0-RC9"
val pureConfigVersion = "0.17.9"
val log4j2Version = "2.25.1"
val log4catsVersion = "2.7.1"
val munitVersion = "1.1.1"
val munitCatsEffectVersion = "1.0.7"
val tapirVersion = "1.11.35"
val openTelemetryVersion = "1.51.0"
val openTelemetryInstrumentationVersion = "2.17.1"
val jsoniterScalaVersion = "2.36.7"

lazy val log4j2Bom = com.here.bom.Bom(
  "org.apache.logging.log4j" % "log4j-bom" % log4j2Version
)
lazy val openTelemetryBom = com.here.bom.Bom(
  "io.opentelemetry" % "opentelemetry-bom-alpha" % s"$openTelemetryVersion-alpha"
)
lazy val openTelemetryInstrumentationBomAlpha = com.here.bom.Bom(
  "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-bom-alpha" % s"$openTelemetryInstrumentationVersion-alpha"
)

val jvmOptions = List(
  "-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
)

lazy val root = (project in file("."))
  .settings(
    log4j2Bom,
    openTelemetryBom,
    openTelemetryInstrumentationBomAlpha
  )
  .settings(
    name := "todo-backend",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterScalaVersion,
      "com.h2database" % "h2" % "2.3.232",
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-jsoniter-scala" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % openTelemetryBom.key.value % Runtime,
      "io.opentelemetry.instrumentation" % "opentelemetry-log4j-appender-2.17" % openTelemetryInstrumentationBomAlpha.key.value % Runtime,
      "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry-java17" % openTelemetryInstrumentationBomAlpha.key.value,
      "org.apache.logging.log4j" % "log4j-core" % log4j2Bom.key.value % Runtime,
      "org.apache.logging.log4j" % "log4j-jul" % log4j2Bom.key.value % Runtime,
      "org.apache.logging.log4j" % "log4j-layout-template-json" % log4j2Bom.key.value % Runtime,
      "org.apache.logging.log4j" % "log4j-slf4j2-impl" % log4j2Bom.key.value % Runtime,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-otel4s-middleware-trace-server" % http4sOtel4sMiddlewareVersion,
      "org.http4s" %% "http4s-otel4s-middleware-metrics" % http4sOtel4sMiddlewareVersion,
      "org.scalameta" %% "munit" % munitVersion % Test,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-h2" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
      "org.typelevel" %% "munit-cats-effect-3" % munitCatsEffectVersion % Test,
      "org.typelevel" %% "otel4s-oteljava" % otel4sVersion
    ),
    dependencyOverrides ++= log4j2Bom.key.value.bomDependencies ++
      openTelemetryInstrumentationBomAlpha.key.value.bomDependencies ++
      openTelemetryBom.key.value.bomDependencies,
    Compile / run / fork := true,
    javaOptions ++= jvmOptions,
    testFrameworks += new TestFramework("munit.Framework"),
    scalacOptions ++= Seq(
      "-Wnonunit-statement",
      "-Wsafe-init",
      "-Wunused:all",
      "-Wvalue-discard",
      "-deprecation",
      "-feature",
      "-source:3.7-migration",
      "-unchecked"
    ),
    jibJvmFlags := jvmOptions,
    jibBaseImage := "eclipse-temurin:21-jre",
    jibRegistry := sys.env.getOrElse("DOCKER_REGISTRY", "missing-DOCKER_REGISTRY-env-var"),
    jibOrganization := sys.env.getOrElse("DOCKER_REPOSITORY", "missing-DOCKER_REPOSITORY-env-var")
  )
