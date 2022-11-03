package io.funkode.arangodb.models

case class GraphCreate(
    name: GraphName,
    edgeDefinitions: List[GraphEdgeDefinition],
    orphanCollections: List[String]
)
