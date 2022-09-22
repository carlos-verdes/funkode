package io.funkode.rest

import zio.*

enum ApiError extends Throwable:
  case BadRequest(request: Option[Any], message: Option[String], cause: Option[Throwable])
  case Forbidden(request: Option[Any], message: Option[String], cause: Option[Throwable])
  case NotFound(id: Option[Any], message: Option[String], cause: Option[Throwable])
  case Conflict(request: Option[Any], message: Option[String], cause: Option[Throwable])
  case NotImplemented(method: String, message: Option[String])
  case Runtime(req: Option[Any], message: Option[String], cause: Option[Throwable])

object ApiError:
  def notFound(id: Any): ApiError = ApiError.NotFound(Some(id), None, None)

  extension [T](effect: IO[ApiError.NotFound, T]) def ifNotFound(run: => Task[T]): Task[T] =
    effect.catchSome { case ApiError.NotFound(_, _, _) => run }
