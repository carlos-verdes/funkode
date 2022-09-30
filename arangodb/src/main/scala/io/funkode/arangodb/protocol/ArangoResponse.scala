/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package protocol

import zio.json.*
import zio.prelude.Covariant

case class ArangoResponse[+T](
    version: Version,
    `type`: MessageType,
    responseCode: Int,
    meta: Map[String, String] = Map.empty,
    body: Option[T] = None
) extends ArangoMessage[T](version, `type`, meta, body)

type ArangoError = ArangoResponse[ArangoResponse.Error]

object ArangoResponse:

  final case class Error(code: Long, error: Boolean, errorNum: Long, errorMessage: String = "")
      derives JsonCodec

  implicit val functor: Covariant[ArangoResponse] = new Covariant[ArangoResponse]:
    override def map[A, B](f: A => B): ArangoResponse[A] => ArangoResponse[B] =
      (a: ArangoResponse[A]) => a.copy(body = a.body.map(f))
