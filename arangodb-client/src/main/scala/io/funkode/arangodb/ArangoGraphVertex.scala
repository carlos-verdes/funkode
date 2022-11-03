package io.funkode.arangodb

import models.*

trait ArangoGraphVertex[Decoder[_]]:

  def handle: DocumentHandle

  def read[T: Decoder](
      ifNoneMatch: Option[String] = None,
      ifMatch: Option[String] = None
  ): AIO[T]
