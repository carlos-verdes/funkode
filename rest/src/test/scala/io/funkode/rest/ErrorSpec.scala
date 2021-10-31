/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode
package rest

import cats.effect.{IO, MonadThrow, Sync}
import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import org.http4s.{HttpRoutes, Method, Request, Response, Status}
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.http4sLiteralsSyntax
import org.specs2.Specification
import org.specs2.matcher.{IOMatchers, MatchResult}
import org.specs2.specification.core.SpecStructure


trait SomeErrors extends IOMatchers {

  import error._

  def effectWithNotFound[F[_]](implicit F: MonadThrow[F]): F[String] = F.raiseError(notFoundError("someId"))
  def effectWithConflict[F[_]](implicit F: MonadThrow[F]): F[String] = F.raiseError(conflictError("someRequest"))

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

  val service = restErrorMidleware(routesWithErrors[IO])
}

class ErrorSpec
    extends Specification
    with RestRuntimeMatchers[IO]
    with SomeErrors { def is: SpecStructure =
  s2"""
      ApiErrorOps should: <br/>
      Manage not found errors           $manageNotFound
      Manage conflict errors            $manageConflict
      Translate errors into HTTP codes  $manageErrorRestCodes
      """

  import error._

  def manageNotFound: MatchResult[IO[String]] =
    effectWithNotFound[IO].ifNotFound("someString".pure[IO]) must returnValue("someString")

  def manageConflict: MatchResult[IO[String]] =
    effectWithConflict[IO].ifConflict("someString".pure[IO]) must returnValue("someString")

  def manageErrorRestCodes: MatchResult[IO[Response[IO]]] =
    service.orNotFound(Request[IO](Method.GET, uri"/private/customer")) must returnStatus(Status.Forbidden)
}
