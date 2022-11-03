package io.funkode.arangodb

import protocol.*
import models.*

trait ArangoGraphCollection[Decoder[_]]:

  def name: CollectionName

  def vertex(key: DocumentKey): ArangoGraphVertex[Decoder]

  def edge(key: DocumentKey): ArangoGraphEdge[Decoder]

object ArangoGraphCollection:

  import ArangoMessage.*

  class Impl[Encoder[_], Decoder[_]](
      databaseName: DatabaseName,
      graphName: GraphName,
      collectionName: CollectionName
  )(using
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoGraphCollection[Decoder]:

    def name: CollectionName = collectionName

    def vertex(key: DocumentKey): ArangoGraphVertex[Decoder] =
      new ArangoGraphVertex.Impl[Encoder, Decoder](databaseName, graphName, DocumentHandle(name, key))

    def edge(key: DocumentKey): ArangoGraphEdge[Decoder] =
      new ArangoGraphEdge.Impl[Encoder, Decoder](databaseName, graphName, DocumentHandle(name, key))
