/*
 * Copyright 2021 io.freemonads
 *
 * SPDX-License-Identifier: MIT
 */

package avokka.arangodb

import avokka.arangodb.models.GraphInfo.{GraphEdgeDefinition, GraphRepresentation}
import avokka.arangodb.models.{EdgeDefinitionCreate, GraphCreate, GraphInfo}
import avokka.arangodb.protocol.{ArangoClient, ArangoResponse}
import avokka.arangodb.types.DatabaseName

case class GraphResponse(graph: GraphRepresentation)

trait ArangoGraph[F[_]] {

  /** graph name */
  def name: String

  /**
   * Create the named graph
   *
   * @param setup modify creation options
   * @return named graph information
   */
   def create(setup: GraphCreate => GraphCreate = identity): F[ArangoResponse[GraphInfo.Response]]

  /**
   * Return information about collection
   *
   * @return collection information
   */
  def info(): F[ArangoResponse[GraphInfo.Response]]

  def addEdgeDefinition(edgeDefinition: => GraphEdgeDefinition): F[ArangoResponse[GraphInfo.Response]]

  def replaceEdgeDefinition(edgeDefinition: => GraphEdgeDefinition): F[ArangoResponse[GraphInfo.Response]]
}

object ArangoGraph {

  def apply[F[_]: ArangoClient](database: DatabaseName, _name: String): ArangoGraph[F] =
    new ArangoGraph[F] {
      override def name: String = _name

      private val path: String = "/_api/gharial/" + name

      override def info(): F[ArangoResponse[GraphInfo.Response]] = GET(database, path).execute

      override def create(setup: GraphCreate => GraphCreate): F[ArangoResponse[GraphInfo.Response]] = {

        val graphCreate = setup(GraphCreate(name))
        POST(database, "/_api/gharial/", graphCreate.parameters).body(graphCreate).execute
      }

      override def addEdgeDefinition(edgeDefinition: => GraphEdgeDefinition): F[ArangoResponse[GraphInfo.Response]] = {

        POST(database, path + "/edge").body(EdgeDefinitionCreate(edgeDefinition)).execute
      }

      override def replaceEdgeDefinition(ed: => GraphEdgeDefinition): F[ArangoResponse[GraphInfo.Response]] =
        PUT(database, path + "/edge/" + ed.collection).body(EdgeDefinitionCreate(ed)).execute
    }
}
