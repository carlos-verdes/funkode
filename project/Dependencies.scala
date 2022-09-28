import sbt._

object Dependencies {

  object Versions {
    val tapirV = "1.1.0"
    val sttpClientV = "3.7.6"

    val zioV = "2.0.2"
    val zioConfigV = "3.0.2"
    val zioLoggingV = "2.1.0"
    val zioJsonV = "0.3.0"
    val zioConfMagnoliaV = "3.0.2"
    val zioConfTypesafeV = "3.0.2"

    val logBackV = "1.4.0"
    val scalaUriV = "4.0.2"
    val testContainersV = "0.40.10"
  }

  object Libraries {

    import Versions._
    private val mill = "com.softwaremill.sttp"

    val sttpClient          = s"${mill}.client3" %% "zio-json"                 % sttpClientV
    val tapirZioHttpServer  = s"${mill}.tapir"   %% "tapir-zio-http-server"    % tapirV
    val tapirPrometheus     = s"${mill}.tapir"   %% "tapir-prometheus-metrics" % tapirV
    val tapirSwagger        = s"${mill}.tapir"   %% "tapir-swagger-ui-bundle"  % tapirV
    val tapirJsonZio        = s"${mill}.tapir"   %% "tapir-json-zio"           % tapirV
    val tapirSttpStubServer = s"${mill}.tapir"   %% "tapir-sttp-stub-server"   % tapirV

    val zio             = "dev.zio" %% "zio"                 % zioV
    val zioConcurrent   = "dev.zio" %% "zio-concurrent"      % zioV
    val zioConfMagnolia = "dev.zio" %% "zio-config-magnolia" % zioConfMagnoliaV
    val zioConfTypesafe = "dev.zio" %% "zio-config-typesafe" % zioConfTypesafeV
    val zioJson         = "dev.zio" %% "zio-json"            % zioJsonV
    val zioJGolden      = "dev.zio" %% "zio-json-golden"     % zioJsonV
    val zioStreams      = "dev.zio" %% "zio-streams"         % zioV
    val zioTest         = "dev.zio" %% "zio-test"            % zioV
    val zioTestSbt      = "dev.zio" %% "zio-test-sbt"        % zioV

    val logBack        = "ch.qos.logback" % "logback-classic"            % Versions.logBackV
    val testContainers = "com.dimafeng"   %% "testcontainers-scala-core" % Versions.testContainersV
    val scalaUri       = "io.lemonlabs"   %% "scala-uri"                 % scalaUriV
  }
}
