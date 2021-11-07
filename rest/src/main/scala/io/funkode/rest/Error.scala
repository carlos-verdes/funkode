/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode
package rest

import cats.Applicative
import cats.data.{Kleisli, OptionT}
import cats.effect.MonadThrow
import cats.syntax.applicativeError._
import cats.syntax.option._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request, Response}
import org.log4s.getLogger

object error {

  private val logger = getLogger

  trait RestError extends Throwable
  case class BadRequestError(request: Option[Any], message: Option[String], cause: Option[Throwable]) extends RestError
  case class ForbiddenError(request: Option[Any], message: Option[String], cause: Option[Throwable]) extends RestError
  case class NotFoundError(id: Option[Any], message: Option[String], cause: Option[Throwable]) extends RestError
  case class ConflictError(request: Option[Any], message: Option[String], cause: Option[Throwable]) extends RestError
  case class NotImplementedError(method: String, message: Option[String]) extends RestError
  case class RuntimeError(req: Option[Any], message: Option[String], cause: Option[Throwable]) extends RestError

  def requestFormatError(request: Any): RestError = BadRequestError(request.some, None, None)
  def forbiddenError(request: Any): RestError = ForbiddenError(request.some, None, None)
  def notFoundError(id: Any): RestError = NotFoundError(id.some, None, None)
  def conflictError(request: Any): RestError = ConflictError(request.some, None, None)
  def runtimeError(t: Throwable): RestError = RuntimeError(None, None, t.some)

  def restErrorMidleware[F[_]: MonadThrow](service: HttpRoutes[F]): HttpRoutes[F] = Kleisli { (req: Request[F]) =>
    service(req).handleErrorWith(apiErrorToOptionT)
  }

  def apiErrorToOptionT[F[_]: MonadThrow]: Throwable => OptionT[F, Response[F]] =
    (t: Throwable) => OptionT.liftF(apiErrorToResponse[F](t))

  implicit def apiErrorToResponse[F[_]: Applicative](restError: Throwable): F[Response[F]] = {

    val dsl = new Http4sDsl[F] {}
    import dsl._

    logger.error(s"REST API error: $restError")

    restError match {
      case BadRequestError(request, message, cause) =>
        logger.error(s"""Bad request error ${messageDescription(message)}""")
        request.foreach(r => logger.debug(r.toString))
        BadRequest(causeMessage(cause))
      case ForbiddenError(request, message, cause) =>
        logger.error(s"""Forbidden error ${messageDescription(message)}""")
        request.foreach(r => logger.debug(r.toString))
        Forbidden(cause.map(_.getLocalizedMessage).getOrElse(""))
      case NotFoundError(request, message, cause) =>
        logger.error(s"""Not found error ${messageDescription(message)}""")
        request.foreach(r => logger.debug(r.toString))
        NotFound(causeMessage(cause))
      case ConflictError(request, message, cause) =>
        logger.error(s"""Conflict error ${messageDescription(message)}""")
        request.foreach(r => logger.debug(r.toString))
        Conflict(causeMessage(cause))
      case NotImplementedError(method, message) =>
        logger.error(s"""Method $method not implemented ${messageDescription(message)}""")
        NotImplemented(method)
      case RuntimeError(request, message, cause) =>
      logger.error(s"""Runtime error ${messageDescription(message)}""")
        request.foreach(r => logger.debug(r.toString))
        InternalServerError(causeMessage(cause))
      case other =>
        logger.error(other)("other error")
        InternalServerError(causeMessage(other.some))
    }
  }

  def messageDescription(message: Option[String]): String =
    message.map(m => "-> " + m).getOrElse("")

  def causeMessage(cause: Option[Throwable]): String = {
    cause.foreach(logger.error(_)("API error"))
    cause.map(_.getLocalizedMessage).getOrElse("Unknown error")
  }
  
  implicit class ApiErrorOps[F[_]: MonadThrow, R](effect: F[R]) {

    def ifNotFound(handleNotFound: => F[R]): F[R] =
      effect.handleErrorWith {
        case _: NotFoundError => handleNotFound
      }

    def ifConflict(handleConflict: => F[R]): F[R] =
      effect.handleErrorWith {
        case _: ConflictError => handleConflict
      }
  }
}
