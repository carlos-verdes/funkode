import Dependencies._
import Libraries._

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(
  List(
    organization := "io.funkode",
    scalaVersion := "3.2.0",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
  )
)

/*
ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-explain",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Yexplicit-nulls", // experimental (I've seen it cause issues with circe)
    "-Ykind-projector",
    "-Ysafe-init", // experimental (I've seen it cause issues with circe)
  ) ++ Seq("-rewrite", "-indent") ++ Seq("-source", "future-migration")
 */

lazy val commonDependencies = Seq(scalaUri, logBack)
lazy val zioDependencies = Seq(zio, zioJson, zioConcurrent)
lazy val testDependencies = Seq(tapirSttpStubServer, zioTest, zioTestSbt, sttpClient, zioJGolden).map(_ % Test)

lazy val velocypack =
  project
    .in(file("velocypack"))
    .settings(Seq(
      name := "funkode-velocypack"))

lazy val velocystream =
  project
    .in(file("velocystream"))
    .settings(Seq(
      name := "funkode-velocystream",
      libraryDependencies ++= Seq(scodecCore)))
    .dependsOn(velocypack)

lazy val arangodb =
  project
    .in(file("arangodb"))
    .settings(Seq(
      name := "funkode-arangodb",
      libraryDependencies ++= Seq(zioPrelude, zioJson, zioConfMagnolia, zioConfTypesafe) ++ testDependencies),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
    .dependsOn(velocystream)

lazy val arangodbZioStreams =
  project
    .in(file("arangodb-zio-streams"))
    .settings(Seq(
      name := "funkode-arangodb-zio-streams",
      libraryDependencies ++= Seq(zioStreams) ++ testDependencies),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
    .dependsOn(arangodb)

lazy val testcontainers =
  project
    .in(file("testcontainers-zio2-arangodb"))
    .settings(Seq(
      name := "testcontainers-zio2-arangodb",
      libraryDependencies ++= Seq(zio, testContainers, logBack) ++ testDependencies),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
    .dependsOn(arangodb)

lazy val rest =
  project
    .in(file("rest"))
    .settings(Seq(
      name := "funkode-rest",
      libraryDependencies ++= (commonDependencies ++ zioDependencies ++ testDependencies)))

lazy val todo =
  project
    .in(file("examples/todoTasks"))
    .settings(name := "funkode-todo")
    .dependsOn(rest)

addCommandAlias("ll", "projects")
addCommandAlias("checkFmtAll", ";scalafmtSbtCheck;scalafmtCheckAll")
addCommandAlias("testAll", ";compile;test;stryker")
addCommandAlias("sanity", ";compile;scalafmtAll;test;stryker")
