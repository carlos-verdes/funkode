package io.funkode.docker.arangodb

import io.funkode.arangodb.*
import io.funkode.arangodb.models.*
import io.funkode.arangodb.http.json.*
import zio.*
import zio.http.Client
import zio.test.{assert, *}

trait ArangoExamples:

  val testDatabaseName = DatabaseName("ittestdb")
  val testDatabase = DatabaseInfo(testDatabaseName.unwrap, testDatabaseName.unwrap, "", false)

  val petsCollection = CollectionName("pets")

object ArangoDbClientIT extends ZIOSpecDefault with ArangoExamples:

  import codecs.given

  override def spec: Spec[TestEnvironment, Any] =
    suite("ArangoDB client should")(
      test("Connect and login automatically") {
        for serverInfo <- ArangoServerJson.version()
        yield assertTrue(serverInfo == ServerVersion("arango", "community", "3.7.15"))
      },
      test("Create and drop a database") {
        for
          databaseApi <- ArangoDatabaseJson.changeTo(testDatabaseName)
          createResult <- databaseApi.create()
          dataInfo <- databaseApi.info
          deleteResult <- databaseApi.drop
        yield assertTrue(createResult) &&
          assertTrue(dataInfo.name == testDatabase.name) &&
          assertTrue(!dataInfo.isSystem) &&
          assertTrue(deleteResult)
      },
      test("Create a collection (default database)") {
        for
          collection <- ArangoDatabaseJson.collection(petsCollection)
          createdCollection <- collection.create()
          collectionInfo <- collection.info
          collectionChecksum <- collection.checksum()
        yield assertTrue(createdCollection.name == petsCollection) &&
          assertTrue(collectionInfo == createdCollection) &&
          assertTrue(collectionChecksum.name == petsCollection) &&
          assertTrue(!createdCollection.isSystem)
      },
      test("Save documents in a collection") {
        assertTrue(true)
      },
      test("Query documents") {
        assertTrue(true)
      },
      test("Delete documents") {
        assertTrue(true)
      },
      test("Drop a collection (default database)") {
        for
          collection <- ArangoDatabaseJson.collection(petsCollection)
          collectionInfo <- collection.info
          deleteResult <- collection.drop()
        yield assertTrue(deleteResult.id == collectionInfo.id)
      }
    ).provideShared(
      Scope.default,
      ArangoConfiguration.default,
      Client.default,
      ArangodbContainer.life,
      ArangoServerJson.life,
      ArangoDatabaseJson.life
    ) @@ TestAspect.sequential
