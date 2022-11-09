package io.funkode.arangodb

import io.lemonlabs.uri.UrlPath
import io.lemonlabs.uri.typesafe.dsl.*
import zio.*

import io.funkode.velocypack.VPack.*
import models.*
import protocol.*

trait ArangoDatabase[Encoder[_], Decoder[_]]:

  def name: DatabaseName

  def collection(name: CollectionName): ArangoCollection[Encoder, Decoder]

  def document(handle: DocumentHandle): ArangoDocument[Encoder, Decoder]

  def graphs(using Decoder[ArangoResult[GraphList]]): AIO[List[GraphInfo]]

  def graph(graphName: GraphName): ArangoGraph[Encoder, Decoder]

  /*
  def transactions: ArangoTransactions[F]

  def wal: ArangoWal[F]
   */

  def create(users: List[DatabaseCreate.User] = List.empty)(using
      Encoder[DatabaseCreate],
      Decoder[ArangoResult[Boolean]]
  ): AIO[Boolean]

  def info(using Decoder[ArangoResult[DatabaseInfo]]): AIO[DatabaseInfo]

  def drop(using Decoder[ArangoResult[Boolean]]): AIO[Boolean]

  def collections(excludeSystem: Boolean = false)(using
      Decoder[ArangoResult[Result[List[CollectionInfo]]]]
  ): AIO[List[CollectionInfo]]

  def query(query: Query): ArangoQuery[Encoder, Decoder]

  def query(qs: String, bindVars: VObject): ArangoQuery[Encoder, Decoder] = query(Query(qs, bindVars))

  def query(qs: String): ArangoQuery[Encoder, Decoder] = query(qs, VObject.empty)

object ArangoDatabase:

  type DAIO[Encoder[_], Decoder[_], O] = ZIO[ArangoDatabase[Encoder, Decoder], ArangoError, O]

  import ArangoMessage.*

  class Impl[Encoder[_], Decoder[_]](databaseName: DatabaseName)(using
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoDatabase[Encoder, Decoder]:

    def name = databaseName

    override def collection(collectionName: CollectionName): ArangoCollection[Encoder, Decoder] =
      new ArangoCollection.Impl[Encoder, Decoder](this.name, collectionName)(using arangoClient)

    def document(handle: DocumentHandle): ArangoDocument[Encoder, Decoder] =
      new ArangoDocument.Impl[Encoder, Decoder](name, handle)

    def graphs(using Decoder[ArangoResult[GraphList]]): AIO[List[GraphInfo]] =
      GET(name, ApiGharialPath).executeIgnoreResult[GraphList, Encoder, Decoder].map(_.graphs)

    def graph(graphName: GraphName): ArangoGraph[Encoder, Decoder] =
      new ArangoGraph.Impl[Encoder, Decoder](name, graphName)

    def create(users: List[DatabaseCreate.User] = List.empty)(using
        Encoder[DatabaseCreate],
        Decoder[ArangoResult[Boolean]]
    ): AIO[Boolean] =
      POST(DatabaseName.system, ApiDatabase).withBody(DatabaseCreate(name, users)).executeIgnoreResult

    def info(using Decoder[ArangoResult[DatabaseInfo]]): AIO[DatabaseInfo] =
      GET(name, ApiDatabase.addPart("current")).executeIgnoreResult

    def drop(using Decoder[ArangoResult[Boolean]]): AIO[Boolean] =
      DELETE(DatabaseName.system, ApiDatabase.addPart(name.unwrap)).executeIgnoreResult

    def collections(
        excludeSystem: Boolean = false
    )(using Decoder[ArangoResult[Result[List[CollectionInfo]]]]): AIO[List[CollectionInfo]] =
      GET(name, ApiCollectionPath, Map("excludeSystem" -> excludeSystem.toString))
        .executeIgnoreResult[Result[List[CollectionInfo]], Encoder, Decoder]
        .map(_.result)

    def query(query: Query): ArangoQuery[Encoder, Decoder] =
      new ArangoQuery.Impl(name, query)

  def newInstance[Enc[_]: TagK, Dec[_]: TagK](
      databaseName: DatabaseName
  ): RAIO[Enc, Dec, ArangoDatabase[Enc, Dec]] =
    ZIO
      .service[ArangoClient[Enc, Dec]]
      .map(arangoClient => new ArangoDatabase.Impl(databaseName)(using arangoClient))

  def default[Enc[_]: TagK, Dec[_]: TagK]
      : ZLayer[ArangoConfiguration & ArangoClient[Enc, Dec], ArangoError, ArangoDatabase[Enc, Dec]] =
    ZLayer(
      for
        config <- ZIO.service[ArangoConfiguration]
        databaseApi <- newInstance[Enc, Dec](config.database)
      yield databaseApi
    )

  def changeTo[Enc[_]: TagK, Dec[_]: TagK](
      newDatabaseName: DatabaseName
  ): RAIO[Enc, Dec, ArangoDatabase[Enc, Dec]] =
    newInstance(newDatabaseName)

  def collection[Enc[_]: TagK, Dec[_]: TagK](
      collectionName: CollectionName
  ): DAIO[Enc, Dec, ArangoCollection[Enc, Dec]] =
    ZIO.service[ArangoDatabase[Enc, Dec]].map(_.collection(collectionName))
