/*
 * Copyright 2021 io.freemonads
 *
 * SPDX-License-Identifier: MIT
 */

package avokka.arangodb.models

import avokka.arangodb.models.GraphInfo.GraphEdgeDefinition
import avokka.velocypack.VPackEncoder

case class EdgeDefinitionCreate(
    collection: String,
    from: List[String],
    to: List[String]
)

object EdgeDefinitionCreate {

  def apply(ged: GraphEdgeDefinition): EdgeDefinitionCreate = EdgeDefinitionCreate(ged.collection, ged.from, ged.to)

  implicit val encoderEdgeDefinitionCreate: VPackEncoder[EdgeDefinitionCreate] = VPackEncoder.gen
}
