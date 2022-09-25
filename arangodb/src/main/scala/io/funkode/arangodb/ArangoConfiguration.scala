package io.funkode.arangodb

import io.funkode.velocystream.VStreamConfiguration
import zio.config.magnolia.*
import zio.config.*
import zio.config.typesafe.*
import scala.concurrent.duration.TimeUnit

final case class ArangoConfiguration(
    host: String,
    port: Int = 8529,
    username: String,
    password: String,
    chunkLength: Long = VStreamConfiguration.CHUNK_LENGTH_DEFAULT,
    readBufferSize: Int = VStreamConfiguration.READ_BUFFER_SIZE_DEFAULT,
    // connectTimeout: FiniteDuration = VStreamConfiguration.CONNECT_TIMEOUT_DEFAULT,
    // replyTimeout: FiniteDuration = VStreamConfiguration.REPLY_TIMEOUT_DEFAULT,
    database: String = DatabaseName.system.unwrap
) // extends VStreamConfiguration

object ArangoConfiguration:

  val arangoConfigDescriptor = descriptor[ArangoConfiguration].mapKey(toKebabCase)
  val default = TypesafeConfig.fromResourcePath(arangoConfigDescriptor)
