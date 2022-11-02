package io.funkode.arangodb
package http
package json

import zio.{ZIO, ZLayer}
import zio.json.{JsonDecoder, JsonEncoder}

import io.funkode.arangodb.protocol.ArangoClient

type ArangoDatabaseJson = ArangoDatabase[JsonEncoder, JsonDecoder]

object ArangoDatabaseJson:

  import codecs.given
  import models.*

  type JDAIO[O] = ZIO[ArangoDatabaseJson, ArangoError, O]

  def changeTo(databaseName: DatabaseName): JRAIO[ArangoDatabaseJson] =
    ArangoDatabase.changeTo[JsonEncoder, JsonDecoder](databaseName)

  def collection(collectionName: CollectionName): JDAIO[ArangoCollectionJson] =
    ArangoDatabase.collection(collectionName)

  val life: ZLayer[ArangoConfiguration & ArangoClientJson, ArangoError, ArangoDatabaseJson] =
    ArangoDatabase.default[JsonEncoder, JsonDecoder]
