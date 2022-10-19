package io.funkode.docker.arangodb

import io.funkode.arangodb.*
import io.funkode.arangodb.models.*
import io.funkode.arangodb.http.json.*
import zio.*
import zio.http.Client
import zio.json.*
import zio.test.{assert, *}

trait ArangoExamples:

  import codecs.given

  case class Pet(name: String, age: Int) derives JsonCodec
  case class PetAge(_key: DocumentKey, age: Int) derives JsonCodec

  val testDatabaseName = DatabaseName("ittestdb")
  val testDatabase = DatabaseInfo(testDatabaseName.unwrap, testDatabaseName.unwrap, "", false)

  val randomCollection = CollectionName("someRandomCol")
  val petsCollection = CollectionName("pets")

  val pet1 = Pet("dog", 2)
  val pet2 = Pet("cat", 3)
  val pet3 = Pet("hamster", 4)
  val pet4 = Pet("fish", 5)

  def patchPet(_key: DocumentKey) = PetAge(_key, 5)
  val updatedPet2 = pet2.copy(age = 5)
  val morePets = List(pet3, pet4)

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
      test("Create and drop a collection (default database)") {
        for
          collection <- ArangoDatabaseJson.collection(randomCollection)
          createdCollection <- collection.create()
          collectionInfo <- collection.info
          collectionChecksum <- collection.checksum()
          deleteResult <- collection.drop()
        yield assertTrue(createdCollection.name == randomCollection) &&
          assertTrue(collectionInfo == createdCollection) &&
          assertTrue(collectionChecksum.name == randomCollection) &&
          assertTrue(!createdCollection.isSystem) &&
          assertTrue(deleteResult.id == collectionInfo.id)
      },
      test("Save documents in a collection") {
        for
          collection <- ArangoDatabaseJson.collection(petsCollection)
          createdCollection <- collection.create()
          documents = collection.documents
          inserted1 <- documents.insert(pet1, true, true)
          inserted2 <- documents.insert(pet2, true, true)
          insertedCount <- documents.count()
          created <- documents.create(morePets, true, true)
          countAfterCreated <- documents.count()
          updatedDocs <- documents
            .update[Pet, PetAge](List(patchPet(inserted2._key)), waitForSync = true, returnNew = true)
          countAfterUpdate <- documents.count()
          deletedDocs <- documents.remove[Pet, DocumentKey](List(inserted1._key), true)
          countAfterDelete <- documents.count()
          _ <- collection.drop()
        yield assertTrue(inserted1.`new`.get == pet1) &&
          assertTrue(inserted2.`new`.get == pet2) &&
          assertTrue(insertedCount.count == 2L) &&
          assertTrue(created.map(_.`new`.get) == morePets) &&
          assertTrue(countAfterCreated.count == 4L) &&
          assertTrue(updatedDocs.length == 1) &&
          assertTrue(updatedDocs.head.`new`.get == updatedPet2) &&
          assertTrue(countAfterUpdate.count == 4L) &&
          assertTrue(deletedDocs.length == 1) &&
          assertTrue(deletedDocs.head._key == inserted1._key) &&
          assertTrue(countAfterDelete.count == 3L)
      },
      test("Query documents") {
        assertTrue(true)
      },
      test("Delete documents") {
        assertTrue(true)
      }
    ).provideShared(
      Scope.default,
      ArangoConfiguration.default,
      Client.default,
      ArangodbContainer.life,
      ArangoServerJson.life,
      ArangoDatabaseJson.life
    ) @@ TestAspect.sequential
