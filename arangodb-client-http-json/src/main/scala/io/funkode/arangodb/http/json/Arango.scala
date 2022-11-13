package io.funkode.arangodb
package http
package json

import zio.*
import zio.json.*

import io.funkode.arangodb.http.json.ArangoClientJson.initArangoClient
import models.*

type JRAIO[O] = WithClient[JsonEncoder, JsonDecoder, O]

type Arango = ArangoApi[JsonEncoder, JsonDecoder]
type WithJsonApi[O] = WithApi[JsonEncoder, JsonDecoder, O]

type ArangoCollectionJson = ArangoCollection[JsonEncoder, JsonDecoder]
type ArangoDatabaseJson = ArangoDatabase[JsonEncoder, JsonDecoder]
type ArangoGraphJson = ArangoGraph[JsonEncoder, JsonDecoder]
type ArangoServerJson = ArangoServer[JsonDecoder]

object codecs extends Codecs

object Arango:

  def current: WithJsonApi[DatabaseName] = ArangoApi.current

  def changeTo(newDatabaseName: DatabaseName): WithJsonApi[Arango] =
    ArangoApi.changeTo(newDatabaseName)

  def db: WithJsonApi[ArangoDatabaseJson] = ArangoApi.db

  def collection(name: CollectionName): WithJsonApi[ArangoCollectionJson] = ArangoApi.collection(name)

  def server: WithJsonApi[ArangoServerJson] = ArangoApi.server

  val live: ZLayer[ArangoConfiguration & ArangoClientJson, ArangoError, Arango] =
    ZLayer(for
      config <- ZIO.service[ArangoConfiguration]
      arangoClient <- ZIO.service[ArangoClientJson]
    yield new ArangoApi.Impl[JsonEncoder, JsonDecoder](config.database)(using arangoClient))
