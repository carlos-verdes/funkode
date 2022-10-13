/*
 * TODO: License goes here!
 */
package io.funkode.docker.arangodb

import io.funkode.arangodb.*
import zio.*
import zio.http.*
import zio.test.*

abstract class SharedArangoConfigSpec extends ZIOSpec[ArangoConfiguration]:
  override val bootstrap: ZLayer[Any, Nothing, ArangoConfiguration] =
    ArangoConfiguration.default.orDie

/*
abstract class SharedHttpClientSpec extends ZIOSpec[Client]:
  override val bootstrap: ZLayer[Any, Nothing, Client] =
    Client.default.orDie
*/


    /*
    ArangoConfiguration.default.catchAll {
      case t: Throwable =>
        println("Error reading config")
        throw new RuntimeException("Error reading config")
    }
    */
