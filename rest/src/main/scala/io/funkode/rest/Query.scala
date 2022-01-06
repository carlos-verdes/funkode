/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.rest

import cats.tagless.{autoFunctorK, autoSemigroupalK, finalAlg}

object query {


  case class QueryResult[R](results: Vector[R], cursor: Option[String])

  @finalAlg
  @autoFunctorK
  @autoSemigroupalK
  trait QueryDsl[F[_], Des[_]] {

    def query[R](
        query: String,
        batchSize: Option[Long] = None,
        cursor: Option[String] = None)(
        implicit deserializer: Des[R]): F[QueryResult[R]]
  }
}
