addSbtPlugin("com.47deg" % "sbt-microsites" % "1.4.3")
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.8")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.21")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.2")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.2")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.13.1")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.14")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.1")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.1")

dependencyOverrides ++= List(
  "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
)
