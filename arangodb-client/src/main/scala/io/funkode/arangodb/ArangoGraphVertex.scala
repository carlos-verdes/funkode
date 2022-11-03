package io.funkode.arangodb

import models.*
import protocol.*

trait ArangoGraphVertex[Decoder[_]]:

  def handle: DocumentHandle

  def read[T: Decoder](
      ifNoneMatch: Option[String] = None,
      ifMatch: Option[String] = None
  )(using Decoder[ArangoResult[GraphVertex[T]]]): AIO[T]

object ArangoGraphVertex:

  import ArangoMessage.*

  class Impl[Encoder[_], Decoder[_]](
      database: DatabaseName,
      graph: GraphName,
      documentHandle: DocumentHandle
  )(using
      arangoClient: ArangoClient[Encoder, Decoder]
  ) extends ArangoGraphVertex[Decoder]:

    def handle: DocumentHandle = documentHandle

    private val path = ApiGharialPath.addPart(graph.unwrap).addPart("vertex").addParts(handle.path.parts)

    def read[T: Decoder](
        ifNoneMatch: Option[String] = None,
        ifMatch: Option[String] = None
    )(using Decoder[ArangoResult[GraphVertex[T]]]): AIO[T] =
      GET(
        database,
        path,
        meta = Map(
          "If-None-Match" -> ifNoneMatch,
          "If-Match" -> ifMatch
        ).collectDefined
      ).executeIgnoreResult[GraphVertex[T], Encoder, Decoder]
        .map(_.vertex)
