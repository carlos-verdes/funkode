/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.rest

import cats.tagless.finalAlg
import fs2.Stream
import org.http4s.Uri

object query {


  case class QueryResult[R](results: Vector[R], next: Option[Uri])

  @finalAlg
  trait QueryDsl[F[_], Des[_]] {

    def query[R](queryString: String, batchSize: Option[Long] = None)(implicit deserializer: Des[R]): F[QueryResult[R]]
    def next[R](cursor: Uri)(implicit deserializer: Des[R]): F[QueryResult[R]]
    def queryStream[R](queryString: String, bactchSize: Option[Long] = None)(implicit deser: Des[R]): Stream[F, R]
  }
}
