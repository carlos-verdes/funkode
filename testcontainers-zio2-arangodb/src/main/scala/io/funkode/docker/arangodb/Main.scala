/*
 * TODO: License goes here!
 */
package io.funkode.docker.arangodb

import io.funkode.arangodb.*
import io.funkode.arangodb.http.json.*
import zio.*
import zio.Console.*
import zio.json.*
import zio.http.Client
import zio.http.Middleware.*

object Main extends ZIOAppDefault:

  import models.*
  import codecs.given

  def app =
    for
      _ <- printLine("Starting container")
      container <- ZIO.service[ArangodbContainer]
      _ <- printLine("Arango db container started on port " + container.configuration.port)
      _ <- printLine(s"Try using 'root' user and ${container.configuration.password}")
      _ <- printLine(s"http://localhost:${container.configuration.port}/")
      serverInfo <- ArangoServerJson.version(false)
      _ <- printLine(s"""Server info: $serverInfo""")
      _ <- printLine(s"""Creating test2 database""")
      databaseApi <- ArangoDatabaseJson.changeTo(DatabaseName("test2"))
      dbCreated <- databaseApi.create()
      _ <- printLine(s"""Database created? $dbCreated""")
      databaseInfo <- databaseApi.info
      _ <- printLine(s"""Database info $databaseInfo""")
      _ <- printLine(s"""Deleting test2 database""")
      dbDropped <- databaseApi.drop
      _ <- printLine(s"""Database dropped? $dbDropped""")
      _ <- printLine(s"""Press any key to exit""")
      _ <- readLine
    yield ()

  def run = app.provide(
    ArangoConfiguration.default,
    ArangodbContainer.life,
    ArangoServerJson.life,
    Client.default,
    Scope.default
  )
