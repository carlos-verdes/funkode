/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.rest

import cats.tagless._
import org.http4s.Uri

object store {

  import query._
  import resource._
  import resource.ToResource.ops._

  @finalAlg
  trait HttpStoreDsl[F[_], Ser[_], Des[_]] {

    def store[R](uri: Uri, resourceBody: R)(implicit serializer: Ser[R], deserializer: Des[R]): F[HttpResource[R]]
    def linkResources(leftUri: Uri, relType: String, rightUri: Uri, attributes: Map[String, String]): F[Unit]

    def fetchOne[R](uri: Uri)(implicit deserializer: Des[R]): F[HttpResource[R]]
    def fetch[R](uri: Uri)(implicit deserializer: Des[R]): fs2.Stream[F, HttpResource[R]]

    def store[R](res: HttpResource[R])(implicit S: Ser[R], D: Des[R]): F[HttpResource[R]] = store(res.uri, res.body)

    def linkResources[L, R](
        left: HttpResource[L],
        relType: String,
        right: HttpResource[R],
        attributes: Map[String, String] = Map.empty): F[Unit] =
      linkResources(left.uri, relType, right.uri, attributes)

    def store[R: ToResource](body: R)(implicit S: Ser[R], D: Des[R]): F[HttpResource[R]] = store(body.asResource)
  }

  trait HttpStoreWithQueryDsl[F[_], Ser[_], Des[_]] extends HttpStoreDsl[F, Ser, Des] with QueryDsl[F, Des]
}
