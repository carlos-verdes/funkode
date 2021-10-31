# funkode
Main repo for funkode.io

## Scala Functional REST libraries

Helper Scala libraries to work with REST API's on top of [Http4s](https://http4s.org/)


Dependencies:
```scala
object Dependencies {

  object Versions {
    val funkodeRestVersion = "0.0.1"
  }

  object Libraries {
    val funkodeTaglessVersion = "io.funkode" %% "rest" % Versions.funkodeRestVersion
  }
}
```

### Error handling

This library offer an error model and a middleware, it will translate errors into proper HTTP Code responses

ErrorModel:
```Scala
  trait RestError extends Throwable
  case class BadRequestError(...) extends RestError
  case class ForbiddenError(...) extends RestError
  case class NotFoundError(...) extends RestError
  case class ConflictError(...) extends RestError
  case class NotImplementedError(...) extends RestError
  case class RuntimeError(...) extends RestError
```

How to raise an error:
```Scala
  def routesWithErrors[F[_]](implicit F: Sync[F]): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F]{}
    import dsl._

    HttpRoutes.of[F] {

      case GET -> Root / "private" / section =>
        for {
          mockRequest <- F.raiseError[String](forbiddenError(s"Private section: $section"))
          response <- Ok(mockRequest)
        } yield {
          response
        }
    }
  }
```

Only thing you need is to register the middleware:
```Scala
  val service = restErrorMidleware(routesWithErrors[IO]).orNotFound
```