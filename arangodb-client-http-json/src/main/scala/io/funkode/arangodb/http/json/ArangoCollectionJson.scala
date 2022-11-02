/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package http
package json

import zio.json.{JsonDecoder, JsonEncoder}

type ArangoCollectionJson = ArangoCollection[JsonEncoder, JsonDecoder]
