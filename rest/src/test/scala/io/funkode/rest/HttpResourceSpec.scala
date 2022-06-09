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
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Link, LinkValue, Location}
import org.specs2.Specification
import org.specs2.matcher.{IOMatchers, MatchResult, RestMatchers}
import org.specs2.specification.core.SpecStructure

trait SomeServices extends IOMatchers {

  import error._
  import resource._
  import io.funkode.rest.syntax.all._

  case class Mock(name: String, age: Int)
  case class WrongRequest(surname: String)

  val MOCKS_URI = uri"/mocks"

  implicit val mockToResource: ToResource[Mock] = (r: Mock) => MOCKS_URI / r.name

  val mock1 = Mock("name1", 12)
  val mock1Uri = MOCKS_URI / mock1.name
  val mock1Resource = HttpResource(mock1Uri, mock1)


  def storeMockResource[F[_]](uri: Uri, mock: Mock)(implicit F: Applicative[F]): F[HttpResource[Mock]] =
    F.pure(HttpResource(uri / mock.name, mock))

  def routes[F[_]](implicit F: Sync[F]): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F]{}
    import dsl._

    HttpRoutes.of[F] {

      case r @ POST -> Root / "mocks" =>
        for {
          mockResourceWithoutId <- r.as[Mock]
          mockResource <- storeMockResource[F](r.uri, mockResourceWithoutId)
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

  val createMockRequest = service.orNotFound(Request[IO](Method.POST, MOCKS_URI).withEntity(mock1))
  val getMockRequest = service.orNotFound(Request[IO](Method.GET, MOCKS_URI / mock1.name))
  val badRequest = service.orNotFound(Request[IO](Method.POST, MOCKS_URI).withEntity(WrongRequest("asd")))
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
      Create a resource with typeclass ToResource  $resourceFromTypeclass
      """

  import resource._
  import ToResource.ops._

  def parseRequest: MatchResult[IO[Response[IO]]] =
    createMockRequest must returnBody(mock1)

  def okHeaders: MatchResult[IO[Response[IO]]] =
    (getMockRequest must returnBody(mock1)) and
        (getMockRequest must returnContainingHeader(Link(LinkValue(mock1Resource.uri, Some(REL_SELF)))))

  def createdHeaders: MatchResult[IO[Response[IO]]] =
    createMockRequest must returnContainingHeader(Location(mock1Resource.uri))

  def badRequestErrors: MatchResult[IO[Response[IO]]] =
    badRequest must returnStatus(Status.BadRequest)

  def resourceFromTypeclass: MatchResult[Any] =
    (mock1.asResource must_=== HttpResource(mock1Uri, mock1)) and (mock1.uri must_=== mock1Uri)
}
