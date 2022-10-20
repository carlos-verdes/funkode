/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import models.*

trait ArangoQuery[Decoder[_]]:

  def withQuery(f: Query => Query): ArangoQuery[Decoder]

  def batchSize(value: Long): ArangoQuery[Decoder] = withQuery(_.copy(batchSize = Some(value)))

  def count(value: Boolean): ArangoQuery[Decoder] = withQuery(_.copy(count = Some(value)))

  def transaction(id: TransactionId): ArangoQuery[Decoder]

  def execute[T: Decoder]: AIO[Cursor[T]]

  def cursor[T: Decoder]: AIO[ArangoCursor[T, Decoder]]

  def stream[F[_], T: Decoder](implicit S: ArangoStream[F, Decoder]): S.S[F, T]
