import sbt._
import Keys._
import Tests._
import play.sbt.Play.autoImport._
import PlayKeys._
import Dependencies._
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._

val previousVersion = "0.9.2"
val buildVersion = "0.10.0"

val projects = Seq("coreCommon", "playJson", "json4sNative", "json4sJackson", "circe", "upickle")
val crossProjects = projects.map(p => Seq(p + "Legacy", p + "Edge")).flatten

addCommandAlias("testAll", crossProjects.map(p => p + "/test").mkString(";", ";", ""))

addCommandAlias("scaladoc", ";coreEdge/doc;playJsonEdge/doc;json4sNativeEdge/doc;circeEdge/doc;upickleEdge/doc;scaladocScript;cleanScript")

addCommandAlias("publish-doc", ";docs/makeSite;docs/ghpagesPushSite")

addCommandAlias("publishCore", ";coreCommonEdge/publishSigned;coreCommonLegacy/publishSigned");
addCommandAlias("publishPlayJson", ";playJsonEdge/publishSigned;playJsonLegacy/publishSigned");
addCommandAlias("publishJson4Native", ";json4sNativeEdge/publishSigned;json4sNativeLegacy/publishSigned");
addCommandAlias("publishJson4Jackson", ";json4sJacksonEdge/publishSigned;json4sJacksonLegacy/publishSigned");
addCommandAlias("publishCirce", ";circeEdge/publishSigned;circeLegacy/publishSigned");
addCommandAlias("publishUpickle", ";upickleEdge/publishSigned;upickleLegacy/publishSigned")
addCommandAlias("publishPlay", ";playEdge/publishSigned");

addCommandAlias("publishAll", ";publishPlayJson;+publishJson4Native;+publishJson4Jackson;+publishCirce;+publishUpickle")

addCommandAlias("release", ";bumpScript;scaladoc;publish-doc;publishAll;sonatypeRelease;pushScript")

lazy val scaladocScript = taskKey[Unit]("Generate scaladoc and copy it to docs site")
scaladocScript := {
  "./scripts/scaladoc.sh "+buildVersion !
}

lazy val bumpScript = taskKey[Unit]("Bump the new version all around")
bumpScript := {
  "./scripts/bump.sh "+previousVersion+" "+buildVersion !
}

lazy val pushScript = taskKey[Unit]("Push to GitHub")
pushScript := {
  "./scripts/pu.sh "+buildVersion !
}

lazy val cleanScript = taskKey[Unit]("Clean tmp files")
cleanScript := {
  "./scripts/clean.sh" !
}

val baseSettings = Seq(
  organization := "com.pauldijou",
  version := buildVersion,
  scalaVersion in ThisBuild := "2.12.0",
  crossScalaVersions := Seq("2.12.0", "2.11.8", "2.10.6"),
  crossVersion := CrossVersion.binary,
  autoAPIMappings := true,
  resolvers ++= Seq(
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"
  ),
  libraryDependencies ++= Seq(Libs.scalatest, Libs.jmockit),
  scalacOptions in (Compile, doc) ++= Seq("-unchecked", "-deprecation"),
  aggregate in test := false,
  fork in test := true,
  parallelExecution in test := false
)

val publishSettings = Seq(
  homepage := Some(url("http://pauldijou.fr/jwt-scala/")),
  organizationHomepage := Some(url("http://pauldijou.github.io/")),
  apiURL := Some(url("http://pauldijou.fr/jwt-scala/api/")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := { _ => false },
  pomExtra := (
    <scm>
      <url>git@github.com:pauldijou/jwt-scala.git</url>
      <connection>scm:git:git@github.com:pauldijou/jwt-scala.git</connection>
    </scm>
    <developers>
      <developer>
        <id>pdi</id>
        <name>Paul Dijou</name>
        <url>http://pauldijou.fr</url>
      </developer>
    </developers>)
)

val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

// Normal published settings
val releaseSettings = baseSettings ++ publishSettings

// Local non-published projects
val localSettings = baseSettings ++ noPublishSettings


val docSettings = Seq(
  site.addMappingsToSiteDir(tut, "_includes/tut"),
  ghpagesNoJekyll := false,
  siteMappings ++= Seq(
    file("README.md") -> "_includes/README.md"
  ),
  git.remoteRepo := "git@github.com:pauldijou/jwt-scala.git",
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md" | "*.scss"
)

lazy val jwtScala = project.in(file("."))
  .settings(localSettings)
  .settings(
    name := "jwt-scala"
  )
  .aggregate(json4sNativeLegacy, json4sNativeEdge, json4sJacksonLegacy, json4sJacksonEdge, circeLegacy, circeEdge, upickleLegacy, upickleEdge)
  .dependsOn(json4sNativeLegacy, json4sNativeEdge, json4sJacksonLegacy, json4sJacksonEdge, circeLegacy, circeEdge, upickleLegacy, upickleEdge)

lazy val docs = project.in(file("docs"))
  .settings(name := "jwt-docs")
  .settings(localSettings)
  .settings(site.settings)
  .settings(ghpages.settings)
  .settings(tutSettings)
  .settings(docSettings)
  .settings(
    libraryDependencies ++= Seq(Libs.playJson, Libs.json4sNative, Libs.circeCore, Libs.circeGeneric, Libs.circeParse, Libs.upickle)
  )
  .dependsOn(json4sNativeEdge, circeEdge, upickleEdge)

lazy val coreLegacy = project.in(file("core/legacy"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-core-legacy-impl",
    libraryDependencies ++= Seq(Libs.apacheCodec)
  )

lazy val coreEdge = project.in(file("core/edge"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-core-impl"
  )

lazy val coreCommonLegacy = project.in(file("core/common"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-core-legacy",
    target <<= target(_ / "legacy"),
    libraryDependencies ++= Seq(Libs.bouncyCastle)
  )
  .aggregate(coreLegacy)
  .dependsOn(coreLegacy % "compile->compile;test->test")

lazy val coreCommonEdge = project.in(file("core/common"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-core",
    target <<= target(_ / "edge"),
    libraryDependencies ++= Seq(Libs.bouncyCastle)
  )
  .aggregate(coreEdge)
  .dependsOn(coreEdge % "compile->compile;test->test")

lazy val jsonCommonLegacy = project.in(file("json/common"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-json-common-legacy",
    target <<= target(_ / "legacy")
  )
  .aggregate(coreCommonLegacy)
  .dependsOn(coreCommonLegacy % "compile->compile;test->test")

lazy val jsonCommonEdge = project.in(file("json/common"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-json-common",
    target <<= target(_ / "edge")
  )
  .aggregate(coreCommonEdge)
  .dependsOn(coreCommonEdge % "compile->compile;test->test")

lazy val playJsonLegacy = project.in(file("json/play-json"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-play-json-legacy",
    target <<= target(_ / "legacy"),
    libraryDependencies ++= Seq(Libs.playJson)
  )
  .aggregate(jsonCommonLegacy)
  .dependsOn(jsonCommonLegacy % "compile->compile;test->test")

lazy val playJsonEdge = project.in(file("json/play-json"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-play-json",
    target <<= target(_ / "edge"),
    libraryDependencies ++= Seq(Libs.playJson)
  )
  .aggregate(jsonCommonEdge)
  .dependsOn(jsonCommonEdge % "compile->compile;test->test")


lazy val circeLegacy = project.in(file("json/circe"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-circe-legacy",
    target <<= target(_ / "legacy"),
    libraryDependencies ++= Seq(Libs.circeCore, Libs.circeGeneric, Libs.circeParse)
  )
  .aggregate(jsonCommonLegacy)
  .dependsOn(jsonCommonLegacy % "compile->compile;test->test")

lazy val circeEdge = project.in(file("json/circe"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-circe",
    target <<= target(_ / "edge"),
    libraryDependencies ++= Seq(Libs.circeCore, Libs.circeGeneric, Libs.circeParse)
  )
  .aggregate(jsonCommonEdge)
  .dependsOn(jsonCommonEdge % "compile->compile;test->test")

lazy val upickleLegacy = project.in(file("json/upickle"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-upickle-legacy",
    target <<= target(_ / "legacy"),
    libraryDependencies ++= Seq(Libs.upickle)
  )
  .aggregate(jsonCommonLegacy)
  .dependsOn(jsonCommonLegacy % "compile->compile;test->test")

lazy val upickleEdge = project.in(file("json/upickle"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-upickle",
    target <<= target(_ / "edge"),
    libraryDependencies ++= Seq(Libs.upickle)
  )
  .aggregate(jsonCommonEdge)
  .dependsOn(jsonCommonEdge % "compile->compile;test->test")


lazy val json4sCommonLegacy = project.in(file("json/json4s-common"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-json4s-common-legacy",
    target <<= target(_ / "legacy"),
    libraryDependencies ++= Seq(Libs.json4sCore)
  )
  .aggregate(jsonCommonLegacy)
  .dependsOn(jsonCommonLegacy % "compile->compile;test->test")

lazy val json4sCommonEdge = project.in(file("json/json4s-common"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-json4s-common",
    target <<= target(_ / "edge"),
    libraryDependencies ++= Seq(Libs.json4sCore)
  )
  .aggregate(jsonCommonEdge)
  .dependsOn(jsonCommonEdge % "compile->compile;test->test")

lazy val json4sNativeLegacy = project.in(file("json/json4s-native"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-json4s-native-legacy",
    target <<= target(_ / "legacy"),
    libraryDependencies ++= Seq(Libs.json4sNative)
  )
  .aggregate(json4sCommonLegacy)
  .dependsOn(json4sCommonLegacy % "compile->compile;test->test")

lazy val json4sNativeEdge = project.in(file("json/json4s-native"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-json4s-native",
    target <<= target(_ / "edge"),
    libraryDependencies ++= Seq(Libs.json4sNative)
  )
  .aggregate(json4sCommonEdge)
  .dependsOn(json4sCommonEdge % "compile->compile;test->test")

lazy val json4sJacksonLegacy = project.in(file("json/json4s-jackson"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-json4s-jackson-legacy",
    target <<= target(_ / "legacy"),
    libraryDependencies ++= Seq(Libs.json4sJackson)
  )
  .aggregate(json4sCommonLegacy)
  .dependsOn(json4sCommonLegacy % "compile->compile;test->test")

lazy val json4sJacksonEdge = project.in(file("json/json4s-jackson"))
  .settings(releaseSettings)
  .settings(
    name := "jwt-json4s-jackson",
    target <<= target(_ / "edge"),
    libraryDependencies ++= Seq(Libs.json4sJackson)
  )
  .aggregate(json4sCommonEdge)
  .dependsOn(json4sCommonEdge % "compile->compile;test->test")
