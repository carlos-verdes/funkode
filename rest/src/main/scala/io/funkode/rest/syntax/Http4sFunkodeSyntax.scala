/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */
package io.funkode.rest.syntax

import cats.MonadThrow
import cats.implicits.toFlatMapOps
import cats.syntax.option._
import io.funkode.rest.error
import io.funkode.rest.resource.HttpResource
import org.http4s.{DecodeFailure, EntityDecoder, Request, Uri}
import org.http4s.headers.LinkValue

trait Http4sFunkodeSyntax {

  implicit class UriFunkodeOps(uri: Uri) {

    def link(rel: Option[String] = None): LinkValue = LinkValue(uri, rel)
    def link(rel: String): LinkValue = link(rel.some)
  }

  implicit class LinkValueFunkodeOps(linkValue: LinkValue) {

    def withRel(newRel: String): LinkValue = linkValue.copy(rel = newRel.some)
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
