package io.funkode.arangodb

import models.*
import protocol.*

trait ArangoGraphEdge[Decoder[_]]:
  def handle: DocumentHandle

  def read[T: Decoder](
      ifNoneMatch: Option[String] = None,
      ifMatch: Option[String] = None
  )(using Decoder[ArangoResult[GraphEdge[T]]]): AIO[T]

object ArangoGraphEdge:

  import ArangoMessage.*

  class Impl[Encoder[_], Decoder[_]](
      database: DatabaseName,
      graph: GraphName,
      documentHandle: DocumentHandle
  )(using
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoGraphEdge[Decoder]:
    def handle: DocumentHandle = documentHandle

    private val path = ApiGharialPath.addPart(graph.unwrap).addPart("edge").addParts(handle.path.parts)

    def read[T: Decoder](
        ifNoneMatch: Option[String] = None,
        ifMatch: Option[String] = None
    )(using Decoder[ArangoResult[GraphEdge[T]]]): AIO[T] =
      GET(
        database,
        path,
        meta = Map(
          "If-None-Match" -> ifNoneMatch,
          "If-Match" -> ifMatch
        ).collectDefined
      ).executeIgnoreResult[GraphEdge[T], Encoder, Decoder]
        .map(_.edge)
