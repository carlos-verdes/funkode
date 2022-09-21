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

lazy val commonDependencies = Seq(scalaUri)
lazy val zioDependencies = Seq(zio, zioConfig, zioJson)
lazy val testDependencies = Seq(tapirSttpStubServer, zioTest, zioTestSbt, sttpClient, zioJGolden).map(_ % Test)

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
