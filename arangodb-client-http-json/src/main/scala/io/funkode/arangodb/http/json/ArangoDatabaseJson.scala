package io.funkode.arangodb
package http
package json

object ArangoDatabaseJson:

  import codecs.given
  import models.*

  def create(databaseName: DatabaseName): JRAIO[Boolean] = ArangoDatabase.create(databaseName)

  def info(databaseName: DatabaseName): JRAIO[DatabaseInfo] = ArangoDatabase.info(databaseName)

  def drop(databaseName: DatabaseName): JRAIO[Boolean] = ArangoDatabase.drop(databaseName)
