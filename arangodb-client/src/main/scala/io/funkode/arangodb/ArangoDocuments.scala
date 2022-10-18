package io.funkode.arangodb

import models.*

trait ArangoDocuments[Encoder[_], Decoder[_]]:

  def count(transactionId: Option[TransactionId] = None)(
      using
      Decoder[CollectionInfo]
  ): AIO[CollectionCount]

  def insert[T](
      document: T,
      waitForSync: Boolean = false,
      returnNew: Boolean = false,
      returnOld: Boolean = false,
      silent: Boolean = false,
      overwrite: Boolean = false,
      transaction: Option[TransactionId] = None
  )(using
    Encoder[T],
    Decoder[Document[T]]
  ): AIO[Document[T]]

  def create[T](
      documents: List[T],
      waitForSync: Boolean = false,
      returnNew: Boolean = false,
      returnOld: Boolean = false,
      silent: Boolean = false,
      overwrite: Boolean = false,
      transaction: Option[TransactionId] = None
  )(using
    Encoder[List[T]],
    Decoder[Document[List[T]]]
  ): AIO[List[Document[T]]]

  def replace[T](
      documents: List[T],
      waitForSync: Boolean = false,
      ignoreRevs: Boolean = true,
      returnOld: Boolean = false,
      returnNew: Boolean = false,
      transaction: Option[TransactionId] = None
  )(using
    Encoder[List[T]],
    Decoder[Document[List[T]]]
  ): AIO[List[Document[T]]]

  def update[T, P](
      patch: List[P],
      keepNull: Boolean = false,
      mergeObjects: Boolean = true,
      waitForSync: Boolean = false,
      ignoreRevs: Boolean = true,
      returnOld: Boolean = false,
      returnNew: Boolean = false,
      transaction: Option[TransactionId] = None
  )(using
    Encoder[List[P]],
    Decoder[Document[List[T]]]
  ): AIO[List[Document[T]]]

  def remove[T, K](
      keys: List[K],
      waitForSync: Boolean = false,
      returnOld: Boolean = false,
      ignoreRevs: Boolean = true,
      transaction: Option[TransactionId] = None
  )(using
    Encoder[List[K]],
    Decoder[Document[List[T]]]
  ): AIO[List[Document[T]]]
