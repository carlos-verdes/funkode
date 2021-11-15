/*
 * TODO: License goes here!
 */
package io.funkode.arango

import avokka.arangodb.{ArangoCollection, ArangoDatabase, ArangoDocument, ArangoQuery, ArangoTransactions}
import avokka.arangodb.models.{CollectionInfo, DatabaseCreate, DatabaseInfo, Query}
import avokka.arangodb.protocol.ArangoResponse
import avokka.arangodb.types.{CollectionName, DatabaseName, DocumentHandle}
import avokka.velocypack.VPackEncoder
import cats.Functor
import org.http4s.client.Client

object Http4sArangoDatabase {

  def apply[F[_]: Client : Functor](dbName: DatabaseName): ArangoDatabase[F] = new ArangoDatabase[F[_]] {

  override def name: DatabaseName = dbName

  override def collection(name: CollectionName): ArangoCollection[F[_]] = Http4sArangoCollection(name)

  override def document(handle: DocumentHandle): ArangoDocument[F[_]] = ???

  override def transactions: ArangoTransactions[F[_]] = ???

  override def create(users: DatabaseCreate.User*): F[_][ArangoResponse[Boolean]] = ???

  override def info(): F[_][ArangoResponse[DatabaseInfo]] = ???

  override def drop(): F[_][ArangoResponse[Boolean]] = ???

  override def collections(excludeSystem: Boolean): F[_][ArangoResponse[Vector[CollectionInfo]]] = ???

  override def query[V](query: Query[V])(implicit evidence$1: VPackEncoder[V]): ArangoQuery[F[_], V] = ???
}}
