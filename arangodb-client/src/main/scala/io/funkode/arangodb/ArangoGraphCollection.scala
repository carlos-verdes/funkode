package io.funkode.arangodb

import models.*

trait ArangoGraphCollection[Decoder[_]]:

  def name: CollectionName

  def vertex(key: DocumentKey): ArangoGraphVertex[Decoder]

  def edge(key: DocumentKey): ArangoGraphEdge[Decoder]
