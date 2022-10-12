package io.funkode.arangodb

import io.lemonlabs.uri.UrlPath
import io.lemonlabs.uri.typesafe.dsl.*
import zio.*
import zio.config.ConfigDescriptor.*
import zio.config.magnolia.{descriptor, Descriptor}

import models.*
import protocol.*

opaque type DatabaseName = String

trait ArangoDatabase:

  def name: DatabaseName

  /*
  def collection(name: CollectionName): ArangoCollection[F]

  def document(handle: DocumentHandle): ArangoDocument[F]

  def graphs(): F[ArangoResponse[Vector[GraphInfo]]]

  def graph(graphName: GraphName): ArangoGraph[F]

  def transactions: ArangoTransactions[F]

  def wal: ArangoWal[F]
  */

  def create(createDatabase: DatabaseCreate): AIO[Boolean]

  def info(databaseName: DatabaseName): AIO[DatabaseInfo]

  def drop(databaseName: DatabaseName): AIO[Boolean]

  /*
  def collections(excludeSystem: Boolean = false): F[ArangoResponse[Vector[CollectionInfo]]]

  def query[V: VPackEncoder](query: Query[V]): ArangoQuery[F, V]

  def query[V: VPackEncoder](qs: String, bindVars: V): ArangoQuery[F, V] = self.query(Query(qs, bindVars))

  def query(qs: String): ArangoQuery[F, VObject] = self.query(qs, VObject.empty)
  */

object DatabaseName:

  def apply(name: String): DatabaseName = name
  extension (name: DatabaseName) def unwrap: String = name

  given Descriptor[DatabaseName] = Descriptor.from(string)

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val system = DatabaseName("_system")

object ArangoDatabase:

  import ArangoMessage.*

  def create[Encoder[_] : TagK, Decoder[_] : TagK](
      databaseName: DatabaseName)(
      using Encoder[DatabaseCreate],
      Decoder[ArangoResult[Boolean]]
  ): RAIO[Encoder, Decoder, Boolean] =
    ZIO.serviceWithZIO[ArangoClient[Encoder, Decoder]](
      _.commandBody[DatabaseCreate, ArangoResult[Boolean]](
        POST(DatabaseName.system, ApiDatabase).withBody(DatabaseCreate(databaseName.unwrap))
      ).map(_.result)
    )

  def info[Encoder[_] : TagK, Decoder[_] : TagK](
    databaseName: DatabaseName)(
    using Encoder[DatabaseCreate],
    Decoder[ArangoResult[DatabaseInfo]]
  ): RAIO[Encoder, Decoder, DatabaseInfo] =
    ZIO.serviceWithZIO[ArangoClient[Encoder, Decoder]](
      _.getBody[ArangoResult[DatabaseInfo]](
        GET(databaseName, ApiDatabase.addPart("current"))
      ).map(_.result)
    )

  def drop[Encoder[_] : TagK, Decoder[_] : TagK](
    databaseName: DatabaseName)(
    using Encoder[DatabaseCreate],
    Decoder[ArangoResult[Boolean]]
  ): RAIO[Encoder, Decoder, Boolean] =
    ZIO.serviceWithZIO[ArangoClient[Encoder, Decoder]](
      _.getBody[ArangoResult[Boolean]](
        DELETE(databaseName, ApiDatabase.addPart(databaseName.unwrap))
      ).map(_.result)
    )
