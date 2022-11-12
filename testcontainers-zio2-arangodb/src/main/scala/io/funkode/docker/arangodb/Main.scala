/*
 * TODO: License goes here!
 */
package io.funkode.docker.arangodb

import zio.*
import zio.Console.*
import zio.http.Client
import zio.http.Middleware.*
import zio.json.*

import io.funkode.arangodb.*
import io.funkode.arangodb.http.json.*

object Main extends ZIOAppDefault:

  import models.*
  import codecs.given

  case class Rel(_rel: String, _from: DocumentHandle, _to: DocumentHandle) derives JsonCodec

  val testDb = DatabaseName("test")
  val politics = GraphName("politics")
  val allies = CollectionName("allies")
  val countries = CollectionName("countries")
  val graphEdgeDefinitions = List(GraphEdgeDefinition(allies, List(countries), List(countries)))
  val es = DocumentHandle(countries, DocumentKey("ES"))
  val fr = DocumentHandle(countries, DocumentKey("FR"))
  val us = DocumentHandle(countries, DocumentKey("US"))
  val alliesOfEs = List(Rel("ally", es, fr), Rel("ally", es, us), Rel("ally", us, fr))

  def app =
    for
      _ <- printLine("Starting container")
      container <- ZIO.service[ArangodbContainer]
      _ <- printLine("Arango db container started on port " + container.configuration.port)
      _ <- printLine(s"Try using 'root' user and ${container.configuration.password}")
      _ <- printLine(s"http://localhost:${container.configuration.port}/")
      serverInfo <- Arango.server.version()
      _ <- printLine(s"""Server info: $serverInfo""")
      db <- Arango.changeTo(testDb).db
      graph = db.graph(politics)
      graphCreated <- graph.create(graphEdgeDefinitions)
      alliesCol = db.collection(allies)
      _ <- alliesCol.documents.create(alliesOfEs)
      _ <- printLine(s"""Graph created: $graphCreated""")
      _ <- printLine(s"""Press any key to exit""")
      _ <- readLine
    yield ()

  def run = app.provide(
    ArangoConfiguration.default,
    ArangodbContainer.life,
    Arango.life,
    Client.default,
    Scope.default
  )
