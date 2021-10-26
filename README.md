# funkode
Main repo for funkode.io

## Scala Functional REST libraries

Helper Scala libraries to work with REST API's on top of [Http4s](https://http4s.org/)


Tagless REST dependencies:
```scala
object Dependencies {

  object Versions {
    val funkodeTaglessVersion = "0.0.1"
  }

  object Libraries {
    val funkodeTaglessVersion = "io.funkode" %% "tagless" % Versions.funkodeTaglessVersion
  }
}
```