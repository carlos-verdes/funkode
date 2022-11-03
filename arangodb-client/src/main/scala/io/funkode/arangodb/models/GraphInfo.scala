package io.funkode.arangodb.models

case class GraphInfo(
    _id: String,
    _rev: String,
    name: GraphName,
    edgeDefinitions: List[GraphEdgeDefinition] = List.empty,
    minReplicationFactor: Option[Int] = None,
    numberOfShards: Option[Int] = None,
    orphanCollections: List[CollectionName] = List.empty,
    replicationFactor: Option[Int] = None,
    isSmart: Boolean = false,
    smartGraphAttribute: Option[String] = None
)
