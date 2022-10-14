package io.funkode.arangodb.models

final case class CollectionChecksum(
  name: CollectionName,
  checksum: String,
  revision: String
)
