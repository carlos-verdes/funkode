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

  @finalAlg
  trait HttpStoreDsl[F[_], Ser[_], Des[_]] {

    def store[R](uri: Uri, resourceBody: R)(implicit serializer: Ser[R], deserializer: Des[R]): F[HttpResource[R]]
    def fetch[R](resourceUri: Uri)(implicit deserializer: Des[R]): F[HttpResource[R]]
    def linkResources(leftUri: Uri, rightUri: Uri, relType: String, attributes: Map[String, String]): F[Unit]
    def getRelated[R](uri: Uri, relType: String)(implicit deserializer: Des[R]): fs2.Stream[F, R]

    def store[R](res: HttpResource[R])(implicit S: Ser[R], D: Des[R]): F[HttpResource[R]] = store(res.uri, res.body)

    def linkResources[L, R](
        left: HttpResource[L],
        right: HttpResource[R],
        relType: String,
        attributes: Map[String, String] = Map.empty): F[Unit] =
      linkResources(left.uri, right.uri, relType, attributes)

    def getRelated[L, R](left: HttpResource[L], relType: String)(implicit deserializer: Des[R]): fs2.Stream[F, R] =
      getRelated[R](left.uri, relType)
  }

  trait HttpStoreWithQueryDsl[F[_], Ser[_], Des[_]] extends HttpStoreDsl[F, Ser, Des] with QueryDsl[F, Des]
}
