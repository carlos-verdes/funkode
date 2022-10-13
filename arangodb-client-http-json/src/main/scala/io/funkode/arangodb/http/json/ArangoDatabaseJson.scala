package io.funkode.arangodb
package http
package json

import io.funkode.arangodb.protocol.ArangoClient
import zio.json.{JsonDecoder, JsonEncoder}

object ArangoDatabaseJson:

  import codecs.given
  import models.*

  def apply(databaseName: DatabaseName): JRAIO[ArangoDatabase[JsonEncoder, JsonDecoder]] =
    ArangoClient.databaseApi(databaseName)
