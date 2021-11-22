import Dependencies._
import Libraries._
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.HeaderLicenseStyle
import sbt.ThisBuild

ThisBuild / organization := "io.funkode"
ThisBuild / scalaVersion := "2.13.6"
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / homepage := Some(url("https://github.com/carlos-verdes/funkode"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/carlos-verdes/funkode"), "git@github.com:carlos-verdes/funkode.git"))
ThisBuild / developers := List(Developer("carlos-verdes", "Carlos Verdes", "cverdes@gmail.com", url("https://github.com/carlos-verdes")))
ThisBuild / resolvers ++= Seq(Resolver.sonatypeRepo("releases"))


val http4sLibraries = Seq(http4sdsl, http4sServer, http4sBlazeServer, http4sClient, http4sCirce)
val catsLibraries = Seq(catsCore, catsFree, catsTaglessMacros)
val circeLibraries = Seq(circeGeneric, circeLiteral)
val avokkaLibraries = Seq(avokkaFs2, avokkaVelocipack)
val secLibraries = Seq(tsecSig, tsecMac, web3)
val utils = Seq(estatico, pureConfig)

val codeLibraries = http4sLibraries ++ catsLibraries ++ circeLibraries ++ utils

val logLibraries = Seq(logback, logCatsSlf4j, jansi)
val testLibraries = Seq(specs2Core, specs2Cats)

val dockerLibraries = Seq(dockerTestConfig, dockerTestSpecs2, dockerTestSpotify)
val javaxLibraries = Seq(javaxBind, javaxActivation, jaxbCore, jaxbImpl)

val restLibs = codeLibraries ++ secLibraries ++ logLibraries ++ testLibraries ++ javaxLibraries
val arangoLibs = avokkaLibraries ++ logLibraries ++ testLibraries ++ dockerLibraries

lazy val rest = (project in file("rest"))
  .configs(IntegrationTest)
  .settings(
    name := "rest",
    publishMavenStyle := true,
    headerSettings(Test),
    startYear := Some(2021),
    licenses += ("MIT", new URL("https://opensource.org/licenses/MIT")),
    headerLicenseStyle := HeaderLicenseStyle.SpdxSyntax,
    Defaults.itSettings,
    libraryDependencies ++= restLibs,
    scalacOptions += "-Ymacro-annotations",
    coverageExcludedPackages := """io.funkode.rest.Main; io.funkode.*.autoDerive; org.specs2.*;""",
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.0" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
  )

lazy val arangoVpack = (project in file("arango-vpack"))
    .dependsOn(rest)
    .configs(IntegrationTest)
    .settings(
      name := "arango-vpack",
      publishMavenStyle := true,
      Defaults.itSettings,
      libraryDependencies ++= arangoLibs,
      scalacOptions += "-Ymacro-annotations",
      headerSettings(Test),
      startYear := Some(2021),
      licenses += ("MIT", new URL("https://opensource.org/licenses/MIT")),
      headerLicenseStyle := HeaderLicenseStyle.SpdxSyntax,
      coverageExcludedPackages := """io.funkode.arango.Main; io.funkode.*.autoDerive; avokka.arangodb.*;""",
      addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.0" cross CrossVersion.full),
      addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
    )

lazy val funkode = (project in file("."))
    .aggregate(rest, arangoVpack)
    .settings(
      publishArtifact := false,
      publish / skip := true
    )

addCommandAlias("prepare", ";clean ;headerCreate ;publishSigned")
addCommandAlias("sanity", ";clean ;compile ;scalastyle ;coverage ;test ;it:test ;coverageOff ;coverageReport ;project")

coverageExcludedPackages := """io.funkode.rest.Main; io.funkode.*.autoDerive; org.specs2.*; avokka.arangodb.*"""


ThisBuild / publishMavenStyle := true
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials")

import xerial.sbt.Sonatype._
ThisBuild / sonatypeProjectHosting := Some(GitHubHosting("carlos-verdes", "funkode", "cverdes@gmail.com"))
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

// realease with sbt-release plugin
import ReleaseTransformations._
ThisBuild / releaseCrossBuild := true
ThisBuild / releasePublishArtifactsAction := PgpKeys.publishSigned.value
