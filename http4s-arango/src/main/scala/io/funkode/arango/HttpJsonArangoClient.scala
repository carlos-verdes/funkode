/*
 * TODO: License goes here!
 */
package io.funkode.arango

import avokka.arangodb.{ArangoConfiguration, ArangoDatabase, ArangoServer}
import avokka.arangodb.protocol.{ArangoClient, ArangoError, ArangoRequest, ArangoResponse}
import avokka.arangodb.types.DatabaseName
import cats.MonadThrow
import io.circe.{Decoder, Encoder}
import org.typelevel.log4cats.MessageLogger
import scodec.DecodeResult
import scodec.bits.ByteVector

object HttpJsonArangoClient {

  def apply[F[_]](implicit ev: GenericArangoClient[F, Encoder, Decoder]): GenericArangoClient[F, Encoder, Decoder] = ev


}
