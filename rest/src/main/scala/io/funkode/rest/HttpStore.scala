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
  @autoFunctorK
  @autoSemigroupalK
  trait HttpStoreDsl[F[_], Ser[_], Des[_]] {

    def store[R](uri: Uri, resourceBody: R)(implicit serializer: Ser[R], deserializer: Des[R]): F[HttpResource[R]]
    def fetch[R](resourceUri: Uri)(implicit deserializer: Des[R]): F[HttpResource[R]]
    def linkResources(leftUri: Uri, rightUri: Uri, relType: String): F[Unit]

    def store[R](res: HttpResource[R])(implicit S: Ser[R], D: Des[R]): F[HttpResource[R]] = store(res.uri, res.body)

    def linkResources[L, R](left: HttpResource[L], right: HttpResource[R], relType: String): F[Unit] =
      linkResources(left.uri, right.uri, relType)
  }

  trait HttpStoreWithQueryDsl[F[_], Ser[_], Des[_]] extends HttpStoreDsl[F, Ser, Des] with QueryDsl[F, Des]
}
