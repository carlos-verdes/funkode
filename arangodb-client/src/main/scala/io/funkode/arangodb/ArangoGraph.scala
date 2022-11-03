package io.funkode.arangodb

import models.*

trait ArangoGraph[Encoder[_], Decoder[_]]:

  def name: GraphName

  def create(
      edgeDefinitions: List[GraphEdgeDefinition] = List.empty,
      orphanCollections: List[String] = List.empty,
      waitForSync: Boolean = false
  )(using Encoder[GraphEdgeDefinition], Decoder[ArangoResult[GraphInfo]]): AIO[GraphInfo]

  def info(using Decoder[ArangoResult[GraphInfo]]): AIO[GraphInfo]

  def drop(dropCollections: Boolean = false)(using
      Decoder[ArangoResult[Boolean]]
  ): AIO[Boolean]

  def vertexCollections(): AIO[Vector[CollectionName]]

  def addVertexCollection(collection: CollectionName): AIO[GraphInfo]

  def removeVertexCollection(
      collection: CollectionName,
      dropCollection: Boolean = false
  ): AIO[GraphInfo]

  def collection(collection: CollectionName): ArangoGraphCollection[Decoder]

  def vertex(handle: DocumentHandle): ArangoGraphVertex[Decoder]

  def edge(handle: DocumentHandle): ArangoGraphEdge[Decoder]
