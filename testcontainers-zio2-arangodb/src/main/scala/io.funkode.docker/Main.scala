/*
 * TODO: License goes here!
 */
package io.funkode.docker

import io.funkode.arangodb.*
import io.funkode.arangodb.http.json.*
import zio.*
import zio.Console.*
import zio.json.*
import zio.http.Client
import zio.http.Middleware.*

object Main extends ZIOAppDefault:

  def app =
    for
      _ <- printLine("Starting container")
      config <- ZIO.service[ArangoConfiguration]
      container <- ZIO.service[ArangodbContainer]
      _ <- printLine("Arango db container started on port " + container.configuration.port)
      _ <- printLine(s"Try using 'root' user and ${container.configuration.password}")
      _ <- printLine(s"http://localhost:${container.configuration.port}/")
      client <- ZIO.service[ArangoClientHttpJson]
      _ <- printLine(s"Trying to login")
      loginResult <- client.login(config.username, config.password)
      _ <- printLine(s"""Login result: $loginResult""")
      _ <- client.login(config.username, "").catchSome { case e: models.ArangoError =>
        printLine(s"""bad login expected error: $e""")
      }
      _ <- printLine(s"""Press any key to exit""")
      _ <- readLine
    yield ()

  def run = app.provide(ArangoConfiguration.default, ArangodbContainer.life, Client.default, Scope.default)
