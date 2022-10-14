/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import scala.concurrent.duration.*

import zio.*
import zio.config.*
import zio.test.*
import zio.test.Assertion.*

import models.*

trait ArangoConfigExamples:

  val expectedReferenceConf = ArangoConfiguration(
    host = "testHost",
    port = 18529,
    username = "testUser",
    password = "testPassword",
    database = DatabaseName("testDatabase"),
    chunkLength = 20000,
    readBufferSize = 128000,
    connectTimeout = 3.seconds,
    replyTimeout = 35.seconds
  )

object ArangoConfigurationSpec extends ZIOSpecDefault with ArangoConfigExamples:
  override def spec: Spec[TestEnvironment, Any] =
    suite("ArangoConfiguration should")(
      test("Load configuration from reference.conf HOCON file") {
        for config <- getConfig[ArangoConfiguration]
        yield assertTrue(config == expectedReferenceConf)
      }
    ).provideSomeLayerShared(ArangoConfiguration.default)
