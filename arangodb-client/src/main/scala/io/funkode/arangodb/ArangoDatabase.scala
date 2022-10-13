package io.funkode.arangodb

import io.lemonlabs.uri.UrlPath
import io.lemonlabs.uri.typesafe.dsl.*
import zio.*
import zio.config.ConfigDescriptor.*
import zio.config.magnolia.{descriptor, Descriptor}

import models.*
import protocol.*

opaque type DatabaseName = String

object DatabaseName:

  def apply(name: String): DatabaseName = name
  extension (name: DatabaseName) def unwrap: String = name

  given Descriptor[DatabaseName] = Descriptor.from(string)

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val system = DatabaseName("_system")

trait ArangoDatabase[Encoder[_], Decoder[_]]:

  def database: DatabaseName

  /*
  def collection(name: CollectionName): ArangoCollection[F]

  def document(handle: DocumentHandle): ArangoDocument[F]

  def graphs(): F[ArangoResponse[Vector[GraphInfo]]]

  def graph(graphName: GraphName): ArangoGraph[F]

  def transactions: ArangoTransactions[F]

  def wal: ArangoWal[F]
  */

  def create(
      users: Vector[DatabaseCreate.User] = Vector.empty)(
      using Encoder[DatabaseCreate],
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

    def database = databaseName

    def create(
        users: Vector[DatabaseCreate.User] = Vector.empty)(
        using Encoder[DatabaseCreate],
        Decoder[ArangoResult[Boolean]]
    ): AIO[Boolean] =
      val databaseCreate = DatabaseCreate(database, users)
      arangoClient.commandBody[DatabaseCreate, ArangoResult[Boolean]](
          POST(DatabaseName.system, ApiDatabaseManagement).withBody(databaseCreate)
        ).map(_.result)

    def info(using Decoder[ArangoResult[DatabaseInfo]]): AIO[DatabaseInfo] =
      arangoClient.getBody[ArangoResult[DatabaseInfo]](
          GET(database, ApiDatabaseManagement.addPart("current"))
        ).map(_.result)

    def drop(using Decoder[ArangoResult[Boolean]]): AIO[Boolean] =
      arangoClient.getBody[ArangoResult[Boolean]](
        DELETE(DatabaseName.system, ApiDatabaseManagement.addPart(database.unwrap))
      ).map(_.result)
