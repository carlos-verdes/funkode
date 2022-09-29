/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import zio.*

import models.*
import protocol.*

trait ArangoServer[F[_]]:

  type AIO[A] = IO[ArangoError, ArangoResponse[A]]

  def databases(): AIO[Vector[DatabaseName]]
  def version(details: Boolean = false): AIO[ArangoResponse[ServerVersion]]

  // def engine(): F[ArangoResponse[Engine]]
  // def role(): F[ArangoResponse[ServerRole]]
  // def logLevel(): F[ArangoResponse[AdminLog.Levels]]
  // def logLevel(levels: AdminLog.Levels): F[ArangoResponse[AdminLog.Levels]]
