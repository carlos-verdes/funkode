package io.funkode.arangodb
package http
package json

import zio.{ZIO, ZLayer}
import zio.json.{JsonDecoder, JsonEncoder}

type ArangoGraphJson = ArangoGraph[JsonEncoder, JsonDecoder]
