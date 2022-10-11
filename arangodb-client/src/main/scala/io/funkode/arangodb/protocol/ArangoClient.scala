/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package protocol

import zio.*

import models.*

trait ArangoClient[Encoder[_], Decoder[_]]:
  thisArango =>

  def head(header: ArangoMessage.Header): AIO[ArangoMessage.Header]
  def get[O: Decoder](header: ArangoMessage.Header): AIO[ArangoMessage[O]]
  def command[I: Encoder, O: Decoder](message: ArangoMessage[I]): AIO[ArangoMessage[O]]

  def login(username: String, password: String): AIO[ArangoMessage[Token]]

//  def login(token: String): AIO[ArangoMessage.Result]

//def database(name: DatabaseName): ArangoDatabase[F]

//def system: ArangoDatabase[F]

//def db: ArangoDatabase[F]

object ArangoClient:

  def head[Encoder[_]: TagK, Decoder[_]: TagK](
      header: ArangoMessage.Header
  ): RAIO[Encoder, Decoder, ArangoMessage.Header] =
    ZIO.serviceWithZIO[ArangoClient[Encoder, Decoder]](_.head(header))

  def get[Encoder[_]: TagK, Decoder[_]: TagK, O: Decoder: Tag](
      header: ArangoMessage.Header
  ): RAIO[Encoder, Decoder, ArangoMessage[O]] =
    ZIO.serviceWithZIO[ArangoClient[Encoder, Decoder]](_.get(header))

  def command[Encoder[_]: TagK, Decoder[_]: TagK, I: Encoder: Tag, O: Decoder: Tag](
      message: ArangoMessage[I]
  ): RAIO[Encoder, Decoder, ArangoMessage[O]] =
    ZIO.serviceWithZIO[ArangoClient[Encoder, Decoder]](_.command(message))

  def login[Encoder[_]: TagK, Decoder[_]: TagK](
      username: String,
      password: String
  ): RAIO[Encoder, Decoder, ArangoMessage[Token]] =
    ZIO.serviceWithZIO[ArangoClient[Encoder, Decoder]](_.login(username, password))
