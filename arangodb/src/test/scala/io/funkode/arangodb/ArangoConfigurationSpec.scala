/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import zio.*
import zio.config.*
import zio.test.*
import zio.test.Assertion.*

trait ArangoConfigExamples:

  val expectedReferenceConf = ArangoConfiguration(
    host = "testHost",
    port = 18529,
    username = "testUser",
    password = "testPassword",
    database = "testDatabase",
    chunkLength = 20000,
    readBufferSize = 128000
  ) /*,
      connectTimeout = "10s",
      replyTimeout = "30s")
   */
object ArangoConfigurationSpec extends ZIOSpecDefault with ArangoConfigExamples:
  override def spec: Spec[TestEnvironment, Any] =
    suite("ArangoConfiguration should")(
      test("testing testing") {
        assertTrue(true)
      },
      test("Load configuration from reference.conf HOCON file") {
        for config <- getConfig[ArangoConfiguration]
        yield assertTrue(config == expectedReferenceConf)
      }
    ).provideSomeLayerShared(ArangoConfiguration.default)
