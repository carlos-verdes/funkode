/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode
package rest

import cats.{Applicative, Show}
import cats.data.{Kleisli, OptionT}
import cats.effect.MonadThrow
import cats.syntax.applicativeError._
import cats.syntax.option._
import org.http4s.dsl.Http4sDsl
import org.http4s.{DecodeFailure, HttpRoutes, Request, Response}
import org.log4s.getLogger

object error {

  private val logger = getLogger

  sealed trait RestError extends Throwable {

    override def getMessage: String = errorShow.show(this)
  }

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
  def notImplementedError(method: String): RestError = NotImplementedError(method, None)
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
      case d: DecodeFailure =>
        logger.error(s"""Decode error -> ${d.message} """)
        BadRequest(causeMessage(d.cause))
      case other =>
        logger.error(other)("other error")
        InternalServerError(causeMessage(other.some))
    }
  }

  def messageDescription(message: Option[String]): String =
    message.map(m => "-> " + m).getOrElse("")

  def causeMessage(cause: Option[Throwable]): String = {
    cause.foreach(logger.error(_)("API error"))
    cause.map(c => if(c != null) c.getLocalizedMessage else "Unknown error").getOrElse("Unknown error")
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

  implicit val errorShow: Show[RestError] = new Show[RestError] {

    implicit class OptionLabelOps(opt: Option[Any]) {
      def label(label: Any): String = opt.map(s"$label: " + _).getOrElse("")
    }

    private def showId(id: Option[Any]): String = id.label("id")
    private def showRequest(request: Option[Any]): String = request.label("request")
    private def showMessage(message: Option[Any]): String = message.label("message")
    private def showCause(opt: Option[Throwable]): String = opt.map("cause: " + _.getLocalizedMessage).getOrElse("")

    override def show(error: RestError): String = error match {
      case BadRequestError(r, m, c) => s"BadRequestError ${showRequest(r)} ${showMessage(m)} ${showCause(c)}"
      case ForbiddenError(r, m, c) => s"ForbiddenError ${showRequest(r)} ${showMessage(m)} ${showCause(c)}"
      case NotFoundError(id, m, c) => s"NotFoundError ${showId(id)} ${showMessage(m)} ${showCause(c)}"
      case ConflictError(r, m, c) => s"ConflictError ${showRequest(r)} ${showMessage(m)} ${showCause(c)}"
      case NotImplementedError(method, m) => s"NotImplementedError method: $method ${showMessage(m)}"
      case RuntimeError(r, m, c) => s"RuntimeError ${showRequest(r)} ${showMessage(m)} ${showCause(c)}"
    }
  }
}
