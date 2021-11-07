/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.rest

import cats.implicits.{catsSyntaxOptionId, toFlatMapOps}
import cats.{Functor, MonadThrow}
import org.http4s._
import org.http4s.headers.{Link, LinkValue, Location}


object resource {

  val REL_SELF = "self"

  case class HttpResource[R](uri: Uri, body: R)

  object HttpResource {

    implicit def functorInstance[R]: Functor[HttpResource] =  new Functor[HttpResource] {
      override def map[A, B](resource: HttpResource[A])(f: A => B): HttpResource[B] =
        resource.copy(body = f(resource.body))
    }
  }

  implicit class HttpResourceOps[R](rr: HttpResource[R]) {

    def ok[F[_]](implicit EE: EntityEncoder[F, R]): Response[F] =
      Response(status = Status.Ok)
          .withHeaders(Link(LinkValue(rr.uri, rel = Some(REL_SELF))))
          .withEntity[R](rr.body)

    def created[F[_]](implicit EE: EntityEncoder[F, R]): Response[F] =
      Response(status = Status.Created)
          .withHeaders(Location(rr.uri))
          .withEntity[R](rr.body)
  }

  implicit class RequestOps[F[_]](request: Request[F]) {

    import error._

    def attemptResource[R](implicit F: MonadThrow[F], ED: EntityDecoder[F, R]): F[HttpResource[R]] =
      request
          .attemptAs[R](ED)
          .value
          .flatMap(_ match {
            case Right(parsedResource) => F.pure(HttpResource(request.uri, parsedResource))
            case Left(error) => F.raiseError(decodeFailureToApiError(error))
          })

    def decodeFailureToApiError(decodeFailure: DecodeFailure): Throwable =
      BadRequestError(None, decodeFailure.message.some, decodeFailure.cause)
  }
}
