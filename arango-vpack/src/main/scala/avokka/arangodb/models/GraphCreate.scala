/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package avokka.arangodb.models

import avokka.arangodb.models.GraphInfo.GraphEdgeDefinition
import avokka.arangodb.types.CollectionName
import avokka.velocypack.VPackEncoder


final case class GraphCreate(
    name: String,
    edgeDefinitions: List[GraphEdgeDefinition] = List(),
    orphanCollections: List[CollectionName] = List(),
    isSmart: Boolean = false,
    isDisjoint: Boolean = false,
    options: Option[GraphCreate.Options] = None,
    waitForSync: Int = 1
) {
  def parameters = Map("waitForSync" -> waitForSync.toString)
}

object GraphCreate { self =>

  final case class Options(
      smartGraphAttribute: Option[String] = None,
      numberOfShards: Long = 1,
      replicationFactor: Long = 1,
      writeConcern: Option[Long] = None
  )

  object Options {
    implicit val encoder: VPackEncoder[Options] = VPackEncoder.gen
  }

  implicit val encoderGraphInfo: VPackEncoder[GraphInfo.GraphEdgeDefinition] = VPackEncoder.gen
  implicit val encoder: VPackEncoder[GraphCreate] = VPackEncoder.gen
}
