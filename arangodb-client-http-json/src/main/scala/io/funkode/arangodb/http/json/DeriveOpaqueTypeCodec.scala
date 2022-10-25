package io.funkode.arangodb.http.json

import scala.quoted.*

import zio.json.*
import zio.json.internal.*

object DeriveOpaqueTypeCodec:

  inline def gen[T, S](inline create: S => T, inline unwrap: T => S)(using
      JsonCodec[S]
  ): zio.json.JsonCodec[T] =
    createOpaqueCodec(create, unwrap)

  def createOpaqueCodec[T, S](create: S => T, unwrap: T => S)(using
      JsonCodec[S]
  ) =
    val encoder = JsonEncoder[S].contramap(unwrap)
    val decoder = JsonDecoder[S].map(create)

    JsonCodec(encoder, decoder)
