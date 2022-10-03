/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import models.*
import protocol.*

trait ArangoServer[F[_, _]]:

  type AIO[A] = ArangoResponse[F, A]

  def databases(): AIO[Vector[DatabaseName]]
  def version(details: Boolean = false): AIO[ServerVersion]

  // def engine(): F[ArangoResponse[Engine]]
  // def role(): F[ArangoResponse[ServerRole]]
  // def logLevel(): F[ArangoResponse[AdminLog.Levels]]
  // def logLevel(levels: AdminLog.Levels): F[ArangoResponse[AdminLog.Levels]]
