package io.funkode.arangodb.http.json

import scala.quoted.*

import io.funkode.arangodb.models.StringOpaqueType
import zio.json.*
import zio.json.internal.*

object DeriveOpaqueTypeCodec:

  inline def gen[T, S](inline create: S => T, inline unwrap: T => S)(using
      JsonCodec[S]
  ): zio.json.JsonCodec[T] =
    createOpaqueCodec(create, unwrap)

  inline def gen[T: StringOpaqueType]: zio.json.JsonCodec[T] =
    gen[T, String]((s: String) => summon[StringOpaqueType[T]].apply(s), _.unwrap)(using
      JsonCodec[String]
    )

  def createOpaqueCodec[T, S](create: S => T, unwrap: T => S)(using
      JsonCodec[S]
  ) =
    val encoder = JsonEncoder[S].contramap(unwrap)
    val decoder = JsonDecoder[S].map(create)

    JsonCodec(encoder, decoder)

  inline def inspectType[T]: String =
    ${ inpectTypeCode[T] }

  def inpectTypeCode[T: Type](using Quotes) =
    import quotes.reflect.*

    val typeRep: TypeRepr = TypeRepr.of[T]
    val symbol: Symbol = typeRep.typeSymbol
    println(
      s"""
         |Type on macro:
         |""".stripMargin +
        typeRep.show + "\n" +
        typeRep.show(using Printer.TypeReprStructure) + "\n" +
        symbol + "\n" +
        symbol.companionClass
    )

    '{

      "hello"
    }
