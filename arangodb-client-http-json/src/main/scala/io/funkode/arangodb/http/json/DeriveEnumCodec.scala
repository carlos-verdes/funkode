package io.funkode.arangodb.http.json

import scala.quoted.*

import zio.json.*
import zio.json.internal.*

object DeriveEnumCodec:

  inline def gen[T](inline unwrap: T => Int, inline create: Int => T): zio.json.JsonCodec[T] =
    ${ createOpaqueCodec('create, 'unwrap) }

  def createOpaqueCodec[T](
      create: Expr[Int => T],
      unwrap: Expr[T => Int])(
      using t: Type[T])(using Quotes) =
    '{
        val encoder = JsonEncoder[Int].contramap($unwrap)
        val decoder = JsonDecoder[Int].map($create)

        JsonCodec(encoder, decoder)
    }
