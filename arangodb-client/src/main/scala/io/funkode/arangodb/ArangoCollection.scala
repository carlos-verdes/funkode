package io.funkode.arangodb

import io.funkode.arangodb
import models.*
import protocol.*
import zio.*

trait ArangoCollection[Encoder[_], Decoder[_]]:

  def database: DatabaseName
  def name: CollectionName

  /*
  def documents: ArangoDocuments[F]

  def document(key: DocumentKey): ArangoDocument[F]

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

  class Impl[Encoder[_], Decoder[_]](
      databaseName: DatabaseName,
      collectionName: CollectionName,
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoCollection[Encoder, Decoder]:

    def database: DatabaseName = databaseName
    def name = collectionName

    val path = ApiCollectionPath.addPart(name.unwrap)

    def create(setup: CollectionCreate => CollectionCreate = identity)(using
        Encoder[CollectionCreate],
        Decoder[CollectionInfo]
    ): AIO[CollectionInfo] =
      val options = setup(CollectionCreate(name))
      arangoClient.commandBody[CollectionCreate, CollectionInfo](
        POST(database, ApiCollectionPath, options.parameters).withBody(options)
      )

    def checksum(withRevisions: Boolean = false, withData: Boolean = false)(using
        Decoder[CollectionChecksum]
    ): AIO[CollectionChecksum] =
      arangoClient.getBody[CollectionChecksum](
        GET(
          database,
          path.addPart("checksum"),
          Map(
            "withRevisions" -> withRevisions.toString,
            "withData" -> withData.toString
          )
        )
      )

    def info(using D: Decoder[CollectionInfo]): AIO[CollectionInfo] =
      arangoClient.getBody[CollectionInfo](GET(database, path))

    def drop(isSystem: Boolean = false)(using D: Decoder[DeleteResult]): AIO[DeleteResult] =
      arangoClient.getBody(DELETE(database, path))
