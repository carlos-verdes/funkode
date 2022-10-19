/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package http
package json

import zio.json.*
import zio.json.internal.*

trait ModelCodecs:

  import models.*

  given JsonCodec[ArangoError] = DeriveJsonCodec.gen[ArangoError]
  given result[O](using D: JsonCodec[O]): JsonCodec[ArangoResult[O]] = DeriveJsonCodec.gen[ArangoResult[O]]
  given JsonCodec[CollectionChecksum] = DeriveJsonCodec.gen[CollectionChecksum]
  given JsonCodec[CollectionCount] = DeriveJsonCodec.gen[CollectionCount]
  given JsonCodec[CollectionCreate.KeyOptions] = DeriveJsonCodec.gen[CollectionCreate.KeyOptions]
  given JsonCodec[CollectionCreate] = DeriveJsonCodec.gen[CollectionCreate]
  given JsonCodec[CollectionInfo] = DeriveJsonCodec.gen[CollectionInfo]
  given JsonCodec[DatabaseCreate.User] = DeriveJsonCodec.gen[DatabaseCreate.User]
  given JsonCodec[DatabaseCreate] = DeriveJsonCodec.gen[DatabaseCreate]
  given JsonCodec[DatabaseInfo] = DeriveJsonCodec.gen[DatabaseInfo]
  given JsonCodec[DeleteResult] = DeriveJsonCodec.gen[DeleteResult]
  given doc[T: JsonCodec]: JsonCodec[Document[T]] = DeriveJsonCodec.gen[Document[T]]
  given JsonCodec[ServerVersion] = DeriveJsonCodec.gen[ServerVersion]
  given JsonCodec[Token] = DeriveJsonCodec.gen[Token]
  given JsonCodec[UserPassword] = DeriveJsonCodec.gen[UserPassword]

  // opaque string based types
  given JsonCodec[DatabaseName] = DeriveOpaqueTypeCodec.gen(DatabaseName.unwrap, DatabaseName.apply)
  given JsonCodec[CollectionName] = DeriveOpaqueTypeCodec.gen(CollectionName.unwrap, CollectionName.apply)
  given JsonCodec[DocumentKey] = DeriveOpaqueTypeCodec.gen(DocumentKey.unwrap, DocumentKey.apply)
  given JsonCodec[DocumentRevision] =
    DeriveOpaqueTypeCodec.gen(DocumentRevision.unwrap, DocumentRevision.apply)
  given JsonCodec[TransactionId] = DeriveOpaqueTypeCodec.gen(TransactionId.unwrap, TransactionId.apply)

  // enum based types
  given JsonCodec[CollectionType] =
    DeriveEnumCodec.gen(CollectionType.ordinal, CollectionType.fromOrdinal)
  given JsonCodec[CollectionStatus] =
    DeriveEnumCodec.gen(CollectionStatus.ordinal, CollectionStatus.fromOrdinal)

  // special types
  given JsonCodec[DocumentHandle] =
    DeriveOpaqueTypeCodec.gen(DocumentHandle.unwrap, (s) => DocumentHandle.parse(s).get)
