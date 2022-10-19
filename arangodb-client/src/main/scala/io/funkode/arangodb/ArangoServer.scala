/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import io.lemonlabs.uri.UrlPath
import zio.*
import models.*
import protocol.*

trait ArangoServer[Decoder[_]]:

  import ArangoMessage.*

  // def databases(): AIO[Vector[DatabaseName]]
  def version(details: Boolean = false)(using D: Decoder[ServerVersion]): AIO[ServerVersion]

  // def engine(): F[ArangoResponse[Engine]]
  // def role(): F[ArangoResponse[ServerRole]]
  // def logLevel(): F[ArangoResponse[AdminLog.Levels]]
  // def logLevel(levels: AdminLog.Levels): F[ArangoResponse[AdminLog.Levels]]

object ArangoServer:

  import ArangoMessage.*

  val VersionString = "version"

  val Details = "details"

  class Impl[Encoder[_], Decoder[_]](using
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoServer[Decoder]:

    def version(details: Boolean = false)(using D: Decoder[ServerVersion]): AIO[ServerVersion] =
      GET(DatabaseName.system, ApiVersionPath, parameters = Map(Details -> details.toString)).execute
