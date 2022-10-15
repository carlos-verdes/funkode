package io.funkode.arangodb.models

final case class CollectionInfo(
    id: String,
    name: CollectionName,
    status: CollectionStatus,
    `type`: CollectionType,
    isSystem: Boolean,
    globallyUniqueId: String
)
