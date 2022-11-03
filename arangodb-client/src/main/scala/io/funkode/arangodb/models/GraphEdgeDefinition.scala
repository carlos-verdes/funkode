package io.funkode.arangodb.models

case class GraphEdgeDefinition(
    collection: CollectionName,
    from: List[CollectionName],
    to: List[CollectionName]
)
