package io.funkode.arangodb
package http
package json

import zio.*
import zio.http.Client
import zio.json.*

import models.*

type Arango = ArangoApi[JsonEncoder, JsonDecoder]
type WithJsonApi[O] = WithApi[JsonEncoder, JsonDecoder, O]

type ArangoDatabaseJson = ArangoDatabase[JsonEncoder, JsonDecoder]

object Arango:

  def current: WithJsonApi[DatabaseName] = ArangoApi.current

  def changeTo(newDatabaseName: DatabaseName): WithJsonApi[Arango] =
    ArangoApi.changeTo(newDatabaseName)

  def db: WithJsonApi[ArangoDatabaseJson] = ArangoApi.db

  def collection(name: CollectionName): WithJsonApi[ArangoCollectionJson] = ArangoApi.collection(name)

  def server: WithJsonApi[ArangoServerJson] = ArangoApi.server          

  val life: ZLayer[ArangoConfiguration & ArangoClientJson, ArangoError, Arango] =
    ZLayer(for
      config <- ZIO.service[ArangoConfiguration]
      arangoClient <- ZIO.service[ArangoClientJson]
    yield new ArangoApi.Impl[JsonEncoder, JsonDecoder](config.database)(using arangoClient))
