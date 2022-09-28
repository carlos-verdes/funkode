package io.funkode.arangodb

import scala.concurrent.duration.*
import java.util.concurrent.TimeUnit.*

import io.funkode.velocystream.VStreamConfiguration
import zio.config.magnolia.*
import zio.config.*
import zio.config.typesafe.*

final case class ArangoConfiguration(
    host: String,
    port: Int = 8529,
    username: String,
    password: String,
    chunkLength: Long = VStreamConfiguration.CHUNK_LENGTH_DEFAULT,
    readBufferSize: Int = VStreamConfiguration.READ_BUFFER_SIZE_DEFAULT,
    connectTimeout: Duration = VStreamConfiguration.CONNECT_TIMEOUT_DEFAULT,
    replyTimeout: Duration = VStreamConfiguration.REPLY_TIMEOUT_DEFAULT,
    database: DatabaseName = DatabaseName.system
) extends VStreamConfiguration

object ArangoConfiguration:

  val arangoConfigDescriptor = descriptor[ArangoConfiguration].mapKey(toKebabCase)
  val default = TypesafeConfig.fromResourcePath(arangoConfigDescriptor)
