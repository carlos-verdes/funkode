package io.funkode.arangodb

import io.lemonlabs.uri.UrlPath
import io.lemonlabs.uri.typesafe.dsl.*
import zio.*

import models.*
import protocol.*

trait ArangoDatabase[Encoder[_], Decoder[_]]:

  def name: DatabaseName

  def collectionApi(name: CollectionName): ArangoCollection[Encoder, Decoder]

  /*
  def document(handle: DocumentHandle): ArangoDocument[F]

  def graphs(): F[ArangoResponse[Vector[GraphInfo]]]

  def graph(graphName: GraphName): ArangoGraph[F]

  def transactions: ArangoTransactions[F]

  def wal: ArangoWal[F]
   */

  def create(users: Vector[DatabaseCreate.User] = Vector.empty)(using
      Encoder[DatabaseCreate],
      Decoder[ArangoResult[Boolean]]
  ): AIO[Boolean]

  def info(using Decoder[ArangoResult[DatabaseInfo]]): AIO[DatabaseInfo]

  def drop(using Decoder[ArangoResult[Boolean]]): AIO[Boolean]

  /*
  def collections(excludeSystem: Boolean = false): F[ArangoResponse[Vector[CollectionInfo]]]

  def query[V: VPackEncoder](query: Query[V]): ArangoQuery[F, V]

  def query[V: VPackEncoder](qs: String, bindVars: V): ArangoQuery[F, V] = self.query(Query(qs, bindVars))

  def query(qs: String): ArangoQuery[F, VObject] = self.query(qs, VObject.empty)
   */

object ArangoDatabase:

  import ArangoMessage.*

  class Impl[Encoder[_], Decoder[_]](
      databaseName: DatabaseName,
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoDatabase[Encoder, Decoder]:

    def name = databaseName

    override def collectionApi(collectionName: CollectionName): ArangoCollection[Encoder, Decoder] =
      new ArangoCollection.Impl[Encoder, Decoder](this.name, collectionName, arangoClient)

    def create(users: Vector[DatabaseCreate.User] = Vector.empty)(using
        Encoder[DatabaseCreate],
        Decoder[ArangoResult[Boolean]]
    ): AIO[Boolean] =
      val databaseCreate = DatabaseCreate(name, users)
      arangoClient
        .commandBody[DatabaseCreate, ArangoResult[Boolean]](
          POST(DatabaseName.system, ApiDatabaseManagementPath).withBody(databaseCreate)
        )
        .map(_.result)

    def info(using Decoder[ArangoResult[DatabaseInfo]]): AIO[DatabaseInfo] =
      arangoClient
        .getBody[ArangoResult[DatabaseInfo]](
          GET(name, ApiDatabaseManagementPath.addPart("current"))
        )
        .map(_.result)

    def drop(using Decoder[ArangoResult[Boolean]]): AIO[Boolean] =
      arangoClient
        .getBody[ArangoResult[Boolean]](
          DELETE(DatabaseName.system, ApiDatabaseManagementPath.addPart(name.unwrap))
        )
        .map(_.result)
