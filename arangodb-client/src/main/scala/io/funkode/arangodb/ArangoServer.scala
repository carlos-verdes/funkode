/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import io.funkode.arangodb.protocol.ArangoMessage.GET
import io.lemonlabs.uri.UrlPath
import zio.*
import models.*
import protocol.*

trait ArangoServer:

  // def databases(): AIO[Vector[DatabaseName]]
  def version(details: Boolean = false): IO[ArangoError, ArangoMessage[ServerVersion]]

  // def engine(): F[ArangoResponse[Engine]]
  // def role(): F[ArangoResponse[ServerRole]]
  // def logLevel(): F[ArangoResponse[AdminLog.Levels]]
  // def logLevel(levels: AdminLog.Levels): F[ArangoResponse[AdminLog.Levels]]

object ArangoServer:

  val ApiVersionPath = UrlPath.fromRaw("/_api/version")
  val Details = "details"

  def version[Encoder[_]: TagK, Decoder[_]: TagK](details: Boolean = false)(using
      Decoder[ServerVersion]
  ): RAIO[Encoder, Decoder, ArangoMessage[ServerVersion]] =
    ZIO.serviceWithZIO[ArangoClient[Encoder, Decoder]](
      _.get[ServerVersion](
        GET(DatabaseName.system, ApiVersionPath, parameters = Map(Details -> details.toString))
      )
    )
