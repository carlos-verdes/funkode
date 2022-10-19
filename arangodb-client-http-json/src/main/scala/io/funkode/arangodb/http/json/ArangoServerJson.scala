package io.funkode.arangodb
package http
package json

import zio.*
import zio.json.*

type ArangoServerJson = ArangoServer[JsonDecoder]

object ArangoServerJson:

  import models.*
  import protocol.*
  import codecs.given_JsonCodec_ServerVersion

  type SAIO[O] = ZIO[ArangoServerJson, ArangoError, O]

  def version(details: Boolean = false): SAIO[ServerVersion] =
    ZIO.serviceWithZIO[ArangoServerJson](_.version(details))

  val life: ZLayer[ArangoClientJson, ArangoError, ArangoServerJson] =
    ZLayer(
      for arangoClient <- ZIO.service[ArangoClientJson]
      yield new ArangoServer.Impl[JsonEncoder, JsonDecoder](using arangoClient)
    )
