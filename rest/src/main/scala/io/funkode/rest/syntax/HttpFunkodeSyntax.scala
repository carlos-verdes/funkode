/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */
package io.funkode.rest
package syntax

import io.funkode.rest.resource.HttpResource
import org.http4s.{EntityEncoder, Response, Status}
import org.http4s.headers.{Link, LinkValue, Location}

trait HttpFunkodeSyntax {

  implicit class HttpResourceOps[R](httpResource: HttpResource[R]) {

    def selfLink: LinkValue = HttpResource.selfLink(httpResource.uri)

    def withLinks(newLinks: Vector[LinkValue]): HttpResource[R] =
      httpResource.copy(links = Link(httpResource.links.values ++ newLinks.toList))

    def ok[F[_]](implicit EE: EntityEncoder[F, R]): Response[F] =
      Response(status = Status.Ok)
          .withHeaders(httpResource.links)
          .withEntity[R](httpResource.body)

    def created[F[_]](implicit EE: EntityEncoder[F, R]): Response[F] =
      Response(status = Status.Created)
          .withHeaders(Location(httpResource.uri), httpResource.links)
          .withEntity[R](httpResource.body)

  }
}
