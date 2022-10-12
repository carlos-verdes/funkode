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

  import codecs.given

  def app =
    for
      _ <- printLine("Starting container")
      config <- ZIO.service[ArangoConfiguration]
      container <- ZIO.service[ArangodbContainer]
      _ <- printLine("Arango db container started on port " + container.configuration.port)
      _ <- printLine(s"Try using 'root' user and ${container.configuration.password}")
      _ <- printLine(s"http://localhost:${container.configuration.port}/")
      serverInfo <- ArangoServerJson.version(false)
      _ <- printLine(s"""Server info: $serverInfo""")
      _ <- printLine(s"""Creating test2 database""")
      dbCreated <- ArangoDatabaseJson.create(DatabaseName("test2"))
      _ <- printLine(s"""Database created? $dbCreated""")
      databaseInfo <- ArangoDatabaseJson.info(DatabaseName("test2"))
      _ <- printLine(s"""Database info $databaseInfo""")
      _ <- printLine(s"""Deleting test2 database""")
      dbDropped <- ArangoDatabaseJson.drop(DatabaseName("test2"))
      _ <- printLine(s"""Database dropped? $dbDropped""")

      _ <- printLine(s"""Press any key to exit""")
      _ <- readLine
    yield ()

  def run = app.provide(ArangoConfiguration.default, ArangodbContainer.life, Client.default, Scope.default)
