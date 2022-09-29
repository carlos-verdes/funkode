/*
 * TODO: License goes here!
 */
package io.funkode.docker

import io.funkode.arangodb.ArangoConfiguration
import zio.*
import zio.Console.*

object Main extends ZIOAppDefault:
  def app =
    for
      _ <- printLine("Starting container")
      container <- ZIO.service[ArangodbContainer]
      _ <- printLine("Arango db container started on port " + container.configuration.port)
      _ <- printLine(s"Try using 'root' user and ${container.configuration.password}")
      _ <- printLine(s"http://localhost:${container.configuration.port}/")
      _ <- printLine(s"""Press any key to exit""")
      _ <- readLine
    yield ()

  def run = app.provide(ArangoConfiguration.default, ArangodbContainer.life)
