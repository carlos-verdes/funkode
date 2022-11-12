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

  extension [R, Enc[_], Dec[_]](dbService: ZIO[R, ArangoError, ArangoDatabase[Enc, Dec]])
    def create(users: List[DatabaseCreate.User] = List.empty)(using
        Enc[DatabaseCreate],
        Dec[ArangoResult[Boolean]]
    ): ZIO[R, ArangoError, Boolean] =
      dbService.flatMap(_.create(users))

    def info(using Dec[ArangoResult[DatabaseInfo]]): ZIO[R, ArangoError, DatabaseInfo] =
      dbService.flatMap(_.info)

    def drop(using Dec[ArangoResult[Boolean]]): ZIO[R, ArangoError, Boolean] =
      dbService.flatMap(_.drop)

    def collections(excludeSystem: Boolean = false)(using
        Dec[ArangoResult[Result[List[CollectionInfo]]]]
    ): ZIO[R, ArangoError, List[CollectionInfo]] =
      dbService.flatMap(_.collections(excludeSystem))
