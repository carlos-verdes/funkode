import sbt._

object Dependencies {

  object Versions {
    val scalaUriV = "4.0.2"
    val tapirV = "1.1.0"
    val zioV = "2.0.2"
    val zioConfigV = "3.0.2"
    val zioLoggingV = "2.1.0"
    val sttpClientV = "3.7.6"
    val zioJsonV = "0.3.0"
  }

  object Libraries {

    val scalaUri = "io.lemonlabs" %% "scala-uri" % Versions.scalaUriV

    val tapirZioHttpServer  = "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"    % Versions.tapirV
    val tapirPrometheus     = "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % Versions.tapirV
    val tapirSwagger        = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % Versions.tapirV
    val tapirJsonZio        = "com.softwaremill.sttp.tapir" %% "tapir-json-zio"           % Versions.tapirV
    val tapirSttpStubServer = "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server"   % Versions.tapirV

    val zio        = "dev.zio" %% "zio"             % Versions.zioV
    val zioConfig  = "dev.zio" %% "zio-config"      % Versions.zioConfigV
    val zioJson    = "dev.zio" %% "zio-json"        % Versions.zioJsonV
    val zioJGolden = "dev.zio" %% "zio-json-golden" % Versions.zioJsonV
    val zioTest    = "dev.zio" %% "zio-test"        % Versions.zioV
    val zioTestSbt = "dev.zio" %% "zio-test-sbt"    % Versions.zioV

    val sttpClient = "com.softwaremill.sttp.client3" %% "zio-json" % Versions.sttpClientV
  }
}
