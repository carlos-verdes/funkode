
import sbt._

object Dependencies {

  object Versions {
    val catsV = "2.6.1" // https://github.com/typelevel/cats/releases
    val catsTaglessMacrosV = "0.14.0" // https://github.com/typelevel/cats-tagless
    val fs2Version = "2.5.6" // https://github.com/functional-streams-for-scala/fs2/releases
    val http4sV = "0.22.7" // https://github.com/http4s/http4s/releases
    val circeV = "0.14.1" // https://github.com/circe/circe/releases
    val avokkaV = "0.0.7"
    val tsecV = "0.3.0-M1"
    val web3jVersion = "5.0.0"
    val logCatsSlf4jV = "1.3.1"
    val logBackV = "1.2.3"
    val specs2V = "4.9.3"
    val dockerTestV = "0.9.9"
    val jansiVersion = "1.8"
  }

  object Libraries {
    // val fs2                = "co.fs2"           %% "fs2-core"             % Versions.fs2Version
    // val fs2IO              = "co.fs2"           %% "fs2-io"               % Versions.fs2Version
    val http4sdsl          = "org.http4s"         %% "http4s-dsl"                  % Versions.http4sV
    val http4sServer       = "org.http4s"         %% "http4s-server"               % Versions.http4sV
    val http4sBlazeServer  = "org.http4s"         %% "http4s-blaze-server"         % Versions.http4sV
    val http4sClient       = "org.http4s"         %% "http4s-client"               % Versions.http4sV
    val http4sCirce        = "org.http4s"         %% "http4s-circe"                % Versions.http4sV
    val catsCore           = "org.typelevel"      %% "cats-core"                   % Versions.catsV
    val catsFree           = "org.typelevel"      %% "cats-free"                   % Versions.catsV
    val catsTaglessMacros  = "org.typelevel"      %% "cats-tagless-macros"         % Versions.catsTaglessMacrosV
    val circeCore          = "io.circe"           %% "circe-core"                  % Versions.circeV
    val circeGeneric       = "io.circe"           %% "circe-generic"               % Versions.circeV
    val circeGenericExtras = "io.circe"           %% "circe-generic-extras"        % Versions.circeV
    val circeParser        = "io.circe"           %% "circe-parser"                % Versions.circeV
    val circeLiteral       = "io.circe"           %% "circe-literal"               % Versions.circeV
    val avokkaVelocipack   = "com.bicou"          %% "avokka-velocypack"           % Versions.avokkaV
    val avokkaFs2          = "com.bicou"          %% "avokka-arangodb-fs2"         % Versions.avokkaV
    val tsecMac            = "io.github.jmcardon" %% "tsec-jwt-mac"                % Versions.tsecV
    val tsecSig            = "io.github.jmcardon" %% "tsec-jwt-sig"                % Versions.tsecV
    val web3               = "org.web3j"          %  "core"                        % Versions.web3jVersion

    val logback            = "ch.qos.logback"       %  "logback-classic"         % Versions.logBackV
    val logCatsSlf4j       = "org.typelevel"        %% "log4cats-slf4j"          % Versions.logCatsSlf4jV
    val jansi              = "org.fusesource.jansi" % "jansi"                    % Versions.jansiVersion

    val specs2Core         = "org.specs2"       %% "specs2-core"                 % Versions.specs2V
    val specs2Cats         = "org.specs2"       %% "specs2-cats"                 % Versions.specs2V

    val dockerTestConfig   = "com.whisk"        %% "docker-testkit-config"       % Versions.dockerTestV
    val dockerTestSpecs2   = "com.whisk"        %% "docker-testkit-specs2"       % Versions.dockerTestV % "it, test"
    val dockerTestSpotify  = "com.whisk"        %% "docker-testkit-impl-spotify" % Versions.dockerTestV % "it, test"
    val javaxActivation    = "javax.activation" %  "activation"                  % "1.1.1" % "it"
    val javaxBind          = "javax.xml.bind"   %  "jaxb-api"                    % "2.3.0" % "it"
    val jaxbCore           = "com.sun.xml.bind" %  "jaxb-core"                   % "2.3.0" % "it"
    val jaxbImpl           = "com.sun.xml.bind" %  "jaxb-impl"                   % "2.3.0" % "it"
  }
}
