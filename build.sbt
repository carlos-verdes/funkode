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

ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    //"-explain",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
//    "-Yexplicit-nulls", // experimental (I've seen it cause issues with circe)
    "-Ykind-projector",
//    "-Ysafe-init", // experimental (I've seen it cause issues with circe)
    "-Yretain-trees"
  ) ++ Seq("-rewrite", "-indent") ++ Seq("-source", "future-migration")


lazy val commonDependencies = Seq(scalaUri, logBack, zioPrelude, jansi, zioConfMagnolia, zioConfTypesafe)
lazy val zioDependencies = Seq(zio, zioHttp, zioJson, zioConcurrent, zioConfMagnolia, zioConfTypesafe)
lazy val testDependencies = Seq(tapirSttpStubServer, zioTest, zioTestSbt, sttpClient, zioJGolden).map(_ % Test)

lazy val arangodb =
  project
    .in(file("arangodb-client"))
    .settings(Seq(
      name := "arangodb-client",
      libraryDependencies ++= commonDependencies ++ zioDependencies ++ testDependencies,
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")))

lazy val arangodbHttpJson =
  project
    .in(file("arangodb-client-http-json"))
    .settings(Seq(
      name := "arangodb-client-http-json",
      libraryDependencies ++= commonDependencies ++ zioDependencies ++ testDependencies),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
    .dependsOn(arangodb)

lazy val testcontainers =
  project
    .in(file("testcontainers-zio2-arangodb"))
    .configs(IntegrationTest)
    .settings(Seq(
      name := "testcontainers-zio2-arangodb",
      libraryDependencies ++= Seq(testContainers, logBack) ++ zioDependencies ++ testDependencies),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
    .dependsOn(arangodbHttpJson)

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
