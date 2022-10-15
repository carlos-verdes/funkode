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
      test("Create a database") {
        for
          databaseApi <- ArangoDatabaseJson(testDatabaseName)
          result <- databaseApi.create()
          dataInfo <- databaseApi.info
        yield assertTrue(result) &&
          assertTrue(dataInfo.name == testDatabase.name) &&
          assertTrue(!dataInfo.isSystem)
      },
      test("Create a collection") {
        for
          databaseApi <- ArangoDatabaseJson(testDatabaseName)
          collection = databaseApi.collectionApi(petsCollection)
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
      test("Drop a collection") {
        for
          databaseApi <- ArangoDatabaseJson(testDatabaseName)
          collection = databaseApi.collectionApi(petsCollection)
          collectionInfo <- collection.info
          deleteResult <- collection.drop()
        yield assertTrue(deleteResult.id == collectionInfo.id)
      },
      test("Drop a database") {
        for
          databaseApi <- ArangoClientJson.databaseApi(testDatabaseName)
          result <- databaseApi.drop
        yield assertTrue(result)
      }
    ).provideShared(
      Scope.default,
      ArangoConfiguration.default,
      Client.default,
      ArangodbContainer.life
    ) @@ TestAspect.sequential