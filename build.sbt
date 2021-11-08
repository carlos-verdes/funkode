import Dependencies._
import Libraries._

ThisBuild / organization := "io.funkode"
ThisBuild / scalaVersion := "2.13.6"
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / homepage := Some(url("https://github.com/carlos-verdes/funkode/rest"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/carlos-verdes/funkode/rest"), "git@github.com:carlos-verdes/funkode.git"))
ThisBuild / developers := List(Developer("carlos-verdes", "Carlos Verdes", "cverdes@gmail.com", url("https://github.com/carlos-verdes")))
ThisBuild / resolvers ++= Seq(Resolver.sonatypeRepo("releases"))

val http4sLibraries = Seq(http4sdsl, http4sServer, http4sBlazeServer, http4sClient, http4sCirce)
val catsLibraries = Seq(catsCore, catsFree, catsTaglessMacros)
val circeLibraries = Seq(circeGeneric, circeLiteral)
val avokkaLibraries = Seq(avokkaFs2, avokkaVelocipack)
val secLibraries = Seq(tsecSig, tsecMac, web3)

val codeLibraries = http4sLibraries ++ catsLibraries ++ circeLibraries

val logLibraries = Seq(logback, logCatsSlf4j, jansi)
val testLibraries = Seq(specs2Core, specs2Cats)

val dockerLibraries = Seq(dockerTestConfig, dockerTestSpecs2, dockerTestSpotify)
val javaxLibraries = Seq(javaxBind, javaxActivation, jaxbCore, jaxbImpl)

val restLibs = codeLibraries ++ logLibraries ++ testLibraries ++ javaxLibraries
val arangoLibs = avokkaLibraries ++ logLibraries ++ testLibraries ++ dockerLibraries

lazy val rest = (project in file("rest"))
  .configs(IntegrationTest)
  .settings(
    name := "rest",
    publishMavenStyle := true,
    Defaults.itSettings,
    libraryDependencies ++= restLibs,
    scalacOptions += "-Ymacro-annotations",
    coverageExcludedPackages := """io.funkode.rest.Main; io.funkode.*.autoDerive; org.specs2.*;""",
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.0" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
  )

lazy val arango = (project in file("arango"))
    .dependsOn(rest)
    .configs(IntegrationTest)
    .settings(
      name := "arango",
      publishMavenStyle := true,
      Defaults.itSettings,
      libraryDependencies ++= arangoLibs,
      scalacOptions += "-Ymacro-annotations",
      coverageExcludedPackages := """io.funkode.arango.Main; io.funkode.*.autoDerive; avokka.arangodb.*;""",
      addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.0" cross CrossVersion.full),
      addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
    )

addCommandAlias("prepare", ";clean ;headerCreate ;publishSigned")
addCommandAlias("sanity", ";clean ;compile ;scalastyle ;coverage ;test ;it:test ;coverageOff ;coverageReport ;project")

coverageExcludedPackages := """io.funkode.rest.Main; io.funkode.*.autoDerive; org.specs2.*; avokka.arangodb.*"""

organizationName := "io.funkode"
startYear := Some(2021)
licenses += ("MIT", new URL("https://opensource.org/licenses/MIT"))
headerLicenseStyle := HeaderLicenseStyle.SpdxSyntax
headerSettings(Test)

publishMavenStyle := true
publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials")

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("carlos-verdes", "funkode", "cverdes@gmail.com"))
sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

// realease with sbt-release plugin
import ReleaseTransformations._
releaseCrossBuild := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
