/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package protocol

import models.*

trait ArangoClient[F[_, _], Encoder[_], Decoder[_]]:

  type AIO[A] = ArangoResponse[F, A]

  def send(header: ArangoMessage.Header): AIO[ArangoMessage.Header]
  def send[O: Decoder](header: ArangoMessage.Header): AIO[ArangoMessage[O]]
  def send[I: Encoder, O: Decoder](message: ArangoMessage[I]): AIO[ArangoMessage[O]]

  def login(username: String, password: String): AIO[ArangoMessage[Token]]

//  def login(token: String): AIO[ArangoMessage.Result]

// def server: ArangoServer[F]

//def database(name: DatabaseName): ArangoDatabase[F]

//def system: ArangoDatabase[F]

//def db: ArangoDatabase[F]

