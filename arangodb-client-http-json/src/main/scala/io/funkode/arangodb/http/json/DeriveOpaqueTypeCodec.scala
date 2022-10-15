package io.funkode.arangodb.http.json

import scala.quoted.*

import zio.json.*
import zio.json.internal.*

object DeriveOpaqueTypeCodec:

  inline def gen[T](inline unwrap: T => String, inline create: String => T): zio.json.JsonCodec[T] =
    ${ createOpaqueCodec('create, 'unwrap) }

  def createOpaqueCodec[T](create: Expr[String => T], unwrap: Expr[T => String])(using
      t: Type[T]
  )(using Quotes) =
    '{
      val encoder = JsonEncoder[String].contramap($unwrap)
      val decoder = JsonDecoder[String].map($create)

      JsonCodec(encoder, decoder)
    }
