package io.funkode.arangodb.models

case class ArangoResult[T](error: Boolean, code: Int, result: T)
