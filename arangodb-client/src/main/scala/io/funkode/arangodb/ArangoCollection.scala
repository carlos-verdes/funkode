package io.funkode.arangodb

import zio.*

import io.funkode.arangodb
import models.*
import protocol.*

trait ArangoCollection[Encoder[_], Decoder[_]]:

  def database: DatabaseName
  def name: CollectionName

  def documents: ArangoDocuments[Encoder, Decoder]

  def document(key: DocumentKey): ArangoDocument[Encoder, Decoder]
  /*
  def indexes: ArangoIndexes[F]

  def index(id: String): ArangoIndex[F]
   */
  def create(setup: CollectionCreate => CollectionCreate = identity)(using
      Encoder[CollectionCreate],
      Decoder[CollectionInfo]
  ): AIO[CollectionInfo]

  def checksum(withRevisions: Boolean = false, withData: Boolean = false)(using
      Decoder[CollectionChecksum]
  ): AIO[CollectionChecksum]

  def info(using Decoder[CollectionInfo]): AIO[CollectionInfo]
  /*
  def revision(): F[ArangoResponse[CollectionRevision]]

  def properties(): F[ArangoResponse[CollectionProperties]]

  def update(waitForSync: Option[Boolean] = None,
    schema: Option[CollectionSchema] = None): F[ArangoResponse[CollectionProperties]]

  def truncate(waitForSync: Boolean = false, compact: Boolean = true): F[ArangoResponse[CollectionInfo]]
   */
  def drop(isSystem: Boolean = false)(using D: Decoder[DeleteResult]): AIO[DeleteResult]
/*
  def rename(newName: CollectionName): F[ArangoResponse[CollectionInfo]]

  def all: ArangoQuery[F, VObject]
 */

object ArangoCollection:

  import ArangoMessage.*

  class Impl[Encoder[_], Decoder[_]](databaseName: DatabaseName, collectionName: CollectionName)(using
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoCollection[Encoder, Decoder]:

    def database: DatabaseName = databaseName
    def name = collectionName

    val documents =
      new ArangoDocuments.Impl[Encoder, Decoder](databaseName, collectionName)(using arangoClient)

    def document(documentKey: DocumentKey) =
      new ArangoDocument.Impl[Encoder, Decoder](databaseName, DocumentHandle(this.name, documentKey))(
        using arangoClient
      )

    val path = ApiCollectionPath.addPart(name.unwrap)

    def create(setup: CollectionCreate => CollectionCreate = identity)(using
        Encoder[CollectionCreate],
        Decoder[CollectionInfo]
    ): AIO[CollectionInfo] =
      val options = setup(CollectionCreate(name))
      POST(database, ApiCollectionPath, options.parameters).withBody(options).execute

    def checksum(withRevisions: Boolean = false, withData: Boolean = false)(using
        Decoder[CollectionChecksum]
    ): AIO[CollectionChecksum] =
      GET(
        database,
        path.addPart("checksum"),
        Map(
          "withRevisions" -> withRevisions.toString,
          "withData" -> withData.toString
        )
      ).execute

    def info(using D: Decoder[CollectionInfo]): AIO[CollectionInfo] =
      GET(database, path).execute

    def drop(isSystem: Boolean = false)(using D: Decoder[DeleteResult]): AIO[DeleteResult] =
      DELETE(database, path).execute
