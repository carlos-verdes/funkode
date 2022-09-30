package io.funkode.arangodb
package protocol

class ArangoMessage[+T](
    version: Version = Version.CURRENT,
    `type`: MessageType = MessageType.Request,
    meta: Map[String, String] = Map.empty,
    body: Option[T]
)

object ArangoMessage:

  def apply[T](`type`: MessageType): ArangoMessage[T] =
    new ArangoMessage[T](Version.CURRENT, `type`, Map.empty, None)

  def apply[T](`type`: MessageType, body: T): ArangoMessage[T] =
    new ArangoMessage[T](Version.CURRENT, `type`, Map.empty, Some(body))
