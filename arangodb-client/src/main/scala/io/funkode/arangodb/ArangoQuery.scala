/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import models.*
import protocol.*

trait ArangoQuery[Encoder[_], Decoder[_]]:

  def withQuery(f: Query => Query): ArangoQuery[Encoder, Decoder]

  def batchSize(value: Long): ArangoQuery[Encoder, Decoder] = withQuery(_.copy(batchSize = Some(value)))

  def count(value: Boolean): ArangoQuery[Encoder, Decoder] = withQuery(_.copy(count = Some(value)))

  def transaction(id: TransactionId): ArangoQuery[Encoder, Decoder]

  def execute[T](using Encoder[Query], Decoder[Cursor[T]]): AIO[Cursor[T]]

  def cursor[T](using
      Encoder[Query],
      Decoder[Cursor[T]],
      Decoder[DeleteResult]
  ): AIO[ArangoCursor[Decoder, T]]

  // def stream[F[_], T: Decoder](implicit S: ArangoStream[F, Decoder]): S.S[F, T]

object ArangoQuery:

  import ArangoMessage.*

  case class Options(transaction: Option[TransactionId] = None)

  class Impl[Encoder[_], Decoder[_]](
      database: DatabaseName,
      query: Query,
      options: Options = Options()
  )(using
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoQuery[Encoder, Decoder]:

    def withQuery(f: Query => Query): ArangoQuery[Encoder, Decoder] = new Impl(database, f(query), options)

    override def batchSize(value: Long): ArangoQuery[Encoder, Decoder] = withQuery(
      _.copy(batchSize = Some(value))
    )

    override def count(value: Boolean): ArangoQuery[Encoder, Decoder] = withQuery(_.copy(count = Some(value)))

    def transaction(id: TransactionId): ArangoQuery[Encoder, Decoder] =
      new Impl(database, query, options.copy(transaction = Some(id)))

    def execute[T](using Encoder[Query], Decoder[Cursor[T]]): AIO[Cursor[T]] =
      POST(
        database,
        ApiCursorPath,
        meta = Map(
          Transaction.Key -> options.transaction.map(_.unwrap)
        ).collectDefined
      ).withBody(query).execute[Cursor[T], Encoder, Decoder]

    def cursor[T](using
        Encoder[Query],
        Decoder[Cursor[T]],
        Decoder[DeleteResult]
    ): AIO[ArangoCursor[Decoder, T]] =
      execute.map { resp =>
        ArangoCursor.apply[Encoder, Decoder, T](database, resp, options)
      }
