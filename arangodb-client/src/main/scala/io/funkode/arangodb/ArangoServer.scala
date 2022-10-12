/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import io.lemonlabs.uri.UrlPath
import zio.*

import models.*
import protocol.*

trait ArangoServer:

  // def databases(): AIO[Vector[DatabaseName]]
  def version(details: Boolean = false): AIO[ServerVersion]

  // def engine(): F[ArangoResponse[Engine]]
  // def role(): F[ArangoResponse[ServerRole]]
  // def logLevel(): F[ArangoResponse[AdminLog.Levels]]
  // def logLevel(levels: AdminLog.Levels): F[ArangoResponse[AdminLog.Levels]]

object ArangoServer:

  import ArangoMessage.*

  val VersionString = "version"

  val Details = "details"

  def version[Encoder[_]: TagK, Decoder[_]: TagK](
      details: Boolean = false)(
      using Decoder[ServerVersion]
  ): RAIO[Encoder, Decoder, ServerVersion] =
    ZIO.serviceWithZIO[ArangoClient[Encoder, Decoder]](
      _.getBody[ServerVersion](
        GET(DatabaseName.system, ApiVersion, parameters = Map(Details -> details.toString))
      ))
