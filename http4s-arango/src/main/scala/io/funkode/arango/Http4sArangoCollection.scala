/*
 * TODO: License goes here!
 */
package io.funkode.arango

import avokka.arangodb._
import avokka.arangodb.models._
import avokka.arangodb.protocol.ArangoResponse
import avokka.arangodb.types.{CollectionName, DocumentKey}
import avokka.velocypack.VObject
import cats.Functor
import org.http4s.client.Client

object Http4sArangoCollection {

  def apply[F[_]: Client : Functor](collectionName: CollectionName): ArangoCollection[F] = new ArangoCollection[F] {

  override def name: CollectionName = collectionName

  override def documents: ArangoDocuments[F] = ???

  override def document(key: DocumentKey): ArangoDocument[F] = ???

  override def indexes: ArangoIndexes[F] = ???

  override def index(id: String): ArangoIndex[F] = ???

  override def create(setup: CollectionCreate => CollectionCreate): F[ArangoResponse[CollectionInfo]] = ???

  override def checksum(withRevisions: Boolean, withData: Boolean): F[ArangoResponse[CollectionChecksum]] = ???

  override def info(): F[ArangoResponse[CollectionInfo]] = ???

  override def revision(): F[ArangoResponse[CollectionRevision]] = ???

  override def properties(): F[ArangoResponse[CollectionProperties]] = ???

  override def update(waitForSync: Option[Boolean], schema: Option[CollectionSchema]): F[ArangoResponse[CollectionProperties]] = ???

  override def load(): F[ArangoResponse[CollectionInfo]] = ???

  override def unload(): F[ArangoResponse[CollectionInfo]] = ???

  override def truncate(waitForSync: Boolean, compact: Boolean): F[ArangoResponse[CollectionInfo]] = ???

  override def drop(isSystem: Boolean): F[ArangoResponse[DeleteResult]] = ???

  override def rename(newName: CollectionName): F[ArangoResponse[CollectionInfo]] = ???

  override def all: ArangoQuery[F, VObject] = ???
}}
