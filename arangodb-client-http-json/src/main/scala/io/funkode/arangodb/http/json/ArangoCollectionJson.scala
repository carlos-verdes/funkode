/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package http
package json

import zio.json.{JsonEncoder, JsonDecoder}

type ArangoCollectionJson = ArangoCollection[JsonEncoder, JsonDecoder]
