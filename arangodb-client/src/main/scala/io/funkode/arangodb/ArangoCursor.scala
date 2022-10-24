/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import protocol.*
import models.*

trait ArangoCursor[Decoder[_], T]:
  // def header: ArangoMessage.Header
  def body: Cursor[T]
  def next(): AIO[ArangoCursor[Decoder, T]]
  def delete(): AIO[DeleteResult]

object ArangoCursor:

  import ArangoMessage.*

  def apply[Encoder[_], Decoder[_], T](
      database: DatabaseName,
      cursor: Cursor[T],
      options: ArangoQuery.Options
  )(using
      ArangoClient[Encoder, Decoder],
      Decoder[Cursor[T]],
      Decoder[DeleteResult]
  ): ArangoCursor[Decoder, T] = new ArangoCursor[Decoder, T]:
    // def header: ArangoMessage.Header

    def body: Cursor[T] = cursor

    def next(): AIO[ArangoCursor[Decoder, T]] =
      val op = PUT(
        database,
        ApiCursorPath.addPart(body.id.get),
        meta = Map(
          Transaction.Key -> options.transaction.map(_.unwrap)
        ).collectDefined
      )

      op
        .execute[Cursor[T], Encoder, Decoder]
        .map(cursor => apply(database, cursor, options))

    def delete(): AIO[DeleteResult] =
      DELETE(
        database,
        ApiCursorPath.addPart(body.id.get),
        meta = Map(
          Transaction.Key -> options.transaction.map(_.unwrap)
        ).collectDefined
      ).execute[DeleteResult, Encoder, Decoder]
