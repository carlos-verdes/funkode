/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.rest

import cats.Applicative
import cats.effect.{IO, Sync}
import cats.implicits.{toFlatMapOps, toFunctorOps}
import io.circe.generic.auto._
import io.funkode.rest.resource.REL_SELF
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Link, LinkValue, Location}
import org.http4s.implicits.http4sLiteralsSyntax
import org.specs2.Specification
import org.specs2.matcher.{IOMatchers, MatchResult, RestMatchers}
import org.specs2.specification.core.SpecStructure

trait SomeServices extends IOMatchers {

  import error._
  import resource._

  case class Mock(name: String, age: Int)
  case class WrongRequest(surname: String)

  val mock1 = Mock("name1", 12)
  val mock1Resource = HttpResource(uri"/mocks" / mock1.name, mock1)


  def storeMockResource[F[_]](httpResource: HttpResource[Mock])(implicit F: Applicative[F]): F[HttpResource[Mock]] =
    F.pure(httpResource.copy(uri = httpResource.uri / httpResource.body.name))

  def routes[F[_]](implicit F: Sync[F]): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F]{}
    import dsl._

    HttpRoutes.of[F] {

      case r @ POST -> Root / "mocks" =>
        for {
          mockResourceWithoutId <- r.attemptResource[Mock]
          mockResource <- storeMockResource[F](mockResourceWithoutId)
        } yield {
          mockResource.created[F]
        }

      case r @ GET -> Root / "mocks" / name if name == mock1.name =>
        F.pure(HttpResource(r.uri, mock1).ok[F])

      case GET -> Root / "mocks" / name =>
        F.raiseError(notFoundError(name))
    }
  }

  val service = restErrorMidleware(routes[IO])

  val createMockRequest = service.orNotFound(Request[IO](Method.POST, uri"/mocks").withEntity(mock1))
  val getMockRequest = service.orNotFound(Request[IO](Method.GET, uri"/mocks" / mock1.name))
  val badRequest = service.orNotFound(Request[IO](Method.POST, uri"/mocks").withEntity(WrongRequest("asd")))
}

class HttpResourceSpec
    extends Specification
    with RestMatchers[IO]
    with SomeServices { def is: SpecStructure =
  s2"""
      HttpResource should: <br/>
      Parse a request and create an http resource  $parseRequest
      Create proper `Ok` headers                   $okHeaders
      Create proper `Created` headers              $createdHeaders
      Retrieve bad request errors                  $badRequestErrors
      """

  def parseRequest: MatchResult[IO[Response[IO]]] =
    createMockRequest must returnBody(mock1)

  def okHeaders: MatchResult[IO[Response[IO]]] =
    (getMockRequest must returnBody(mock1)) and
        (getMockRequest must returnContainingHeader(Link(LinkValue(mock1Resource.uri, Some(REL_SELF)))))

  def createdHeaders: MatchResult[IO[Response[IO]]] =
    createMockRequest must returnContainingHeader(Location(mock1Resource.uri))

  def badRequestErrors: MatchResult[IO[Response[IO]]] =
    badRequest must returnStatus(Status.BadRequest)
}
