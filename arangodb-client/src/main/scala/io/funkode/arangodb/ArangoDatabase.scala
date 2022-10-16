package io.funkode.arangodb

import io.lemonlabs.uri.UrlPath
import io.lemonlabs.uri.typesafe.dsl.*
import zio.*
import models.*
import protocol.*

trait ArangoDatabase[Encoder[_], Decoder[_]]:

  def name: DatabaseName

  def collection(name: CollectionName): ArangoCollection[Encoder, Decoder]

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

  type DAIO[Encoder[_], Decoder[_], O] = ZIO[ArangoDatabase[Encoder, Decoder], ArangoError, O]

  import ArangoMessage.*

  class Impl[Encoder[_], Decoder[_]](
      databaseName: DatabaseName,
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoDatabase[Encoder, Decoder]:

    def name = databaseName

    override def collection(collectionName: CollectionName): ArangoCollection[Encoder, Decoder] =
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

  def newInstance[Enc[_]: TagK, Dec[_]: TagK](
      databaseName: DatabaseName
  ): RAIO[Enc, Dec, ArangoDatabase[Enc, Dec]] =
    ZIO
      .service[ArangoClient[Enc, Dec]]
      .map(arangoClient => new ArangoDatabase.Impl(databaseName, arangoClient))

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
