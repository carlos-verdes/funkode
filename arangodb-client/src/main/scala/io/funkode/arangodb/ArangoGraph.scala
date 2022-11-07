package io.funkode.arangodb

import io.funkode.velocypack.*
import models.*
import protocol.*

trait ArangoGraph[Encoder[_], Decoder[_]]:

  def name: GraphName

  def create(
      edgeDefinitions: List[GraphEdgeDefinition] = List.empty,
      orphanCollections: List[String] = List.empty,
      waitForSync: Boolean = false
  )(using Encoder[GraphCreate], Decoder[GraphInfo.Response]): AIO[GraphInfo]

  def info(using Decoder[ArangoResult[GraphInfo.Response]]): AIO[GraphInfo]

  def drop(dropCollections: Boolean = false)(using
      Decoder[ArangoResult[RemovedResult]]
  ): AIO[Boolean]

  def vertexCollections(using Decoder[ArangoResult[GraphCollections]]): AIO[List[CollectionName]]

  def addVertexCollection(
      collection: CollectionName
  )(using Encoder[VertexCollectionCreate], Decoder[ArangoResult[GraphInfo.Response]]): AIO[GraphInfo]

  def removeVertexCollection(
      collection: CollectionName,
      dropCollection: Boolean = false
  )(using Decoder[ArangoResult[GraphInfo.Response]]): AIO[GraphInfo]

  def collection(collection: CollectionName): ArangoGraphCollection[Decoder]

  def vertex(handle: DocumentHandle): ArangoGraphVertex[Decoder]

  def edge(handle: DocumentHandle): ArangoGraphEdge[Decoder]

object ArangoGraph:

  import ArangoMessage.*
  import VPack.*

  class Impl[Encoder[_], Decoder[_]](database: DatabaseName, graphName: GraphName)(using
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoGraph[Encoder, Decoder]:

    def name: GraphName = graphName

    private val path = ApiGharialPath.addPart(graphName.unwrap)
    private val vertexPath = path.addPart("vertex")

    def create(
        edgeDefinitions: List[GraphEdgeDefinition] = List.empty,
        orphanCollections: List[String] = List.empty,
        waitForSync: Boolean = false
    )(using Encoder[GraphCreate], Decoder[GraphInfo.Response]): AIO[GraphInfo] =
      POST(
        database,
        ApiGharialPath,
        Map(
          "waitForSync" -> waitForSync.toString
        )
      ).withBody(
        GraphCreate(name, edgeDefinitions, orphanCollections)
      ).execute[GraphInfo.Response, Encoder, Decoder]
        .map(_.graph)

    def info(using Decoder[ArangoResult[GraphInfo.Response]]): AIO[GraphInfo] =
      GET(database, path)
        .executeIgnoreResult[GraphInfo.Response, Encoder, Decoder]
        .map(_.graph)

    def drop(dropCollections: Boolean = false)(using
        Decoder[ArangoResult[RemovedResult]]
    ): AIO[Boolean] =
      DELETE(database, path, Map("dropCollections" -> dropCollections.toString))
        .executeIgnoreResult[RemovedResult, Encoder, Decoder]
        .map(_.removed)

    def vertexCollections(using Decoder[ArangoResult[GraphCollections]]): AIO[List[CollectionName]] =
      GET(database, vertexPath)
        .executeIgnoreResult[GraphCollections, Encoder, Decoder]
        .map(_.collections)

    def addVertexCollection(
        collection: CollectionName
    )(using Encoder[VertexCollectionCreate], Decoder[ArangoResult[GraphInfo.Response]]): AIO[GraphInfo] =
      POST(database, vertexPath)
        .withBody(VertexCollectionCreate(collection))
        .executeIgnoreResult[GraphInfo.Response, Encoder, Decoder]
        .map(_.graph)

    def removeVertexCollection(
        collection: CollectionName,
        dropCollection: Boolean = false
    )(using Decoder[ArangoResult[GraphInfo.Response]]): AIO[GraphInfo] =
      DELETE(
        database,
        vertexPath.addPart(collection.unwrap),
        Map(
          "dropCollection" -> dropCollection.toString
        )
      ).executeIgnoreResult[GraphInfo.Response, Encoder, Decoder].map(_.graph)

    def collection(collection: CollectionName): ArangoGraphCollection[Decoder] =
      new ArangoGraphCollection.Impl[Encoder, Decoder](database, name, collection)

    def vertex(handle: DocumentHandle): ArangoGraphVertex[Decoder] =
      new ArangoGraphVertex.Impl[Encoder, Decoder](database, name, handle)

    def edge(handle: DocumentHandle): ArangoGraphEdge[Decoder] =
      new ArangoGraphEdge.Impl[Encoder, Decoder](database, name, handle)
