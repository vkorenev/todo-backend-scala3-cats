addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.3")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.5")
addSbtPlugin("de.gccc.sbt" % "sbt-jib" % "1.4.2")
addSbtPlugin("com.here.platform" % "sbt-bom" % "1.0.25")
libraryDependencies += "com.google.cloud.tools" % "jib-core" % "0.27.3"
