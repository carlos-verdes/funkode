package io.funkode.arangodb.models

import zio.config.ConfigDescriptor.string
import zio.config.magnolia.Descriptor

opaque type CollectionName = String
opaque type DocumentKey = String
opaque type DocumentHandle = (CollectionName, DocumentKey)
opaque type DocumentRevision = String
opaque type DatabaseName = String
opaque type TransactionId = String

object CollectionName:

  def apply(name: String): CollectionName = name
  extension (name: CollectionName) def unwrap: String = name

  import zio.config.ConfigDescriptor.string
  import zio.config.magnolia.Descriptor

  given Descriptor[CollectionName] = Descriptor.from(string)

object DocumentHandle:

  val key = "_key"

  def apply(col: CollectionName, key: DocumentKey): DocumentHandle = (col, key)

  def parse(path: String): Option[DocumentHandle] =
    path.split('/') match
      case Array(collection, key) => Some(apply(CollectionName(collection), DocumentKey(key)))
      case _                      => None

  extension (handle: DocumentHandle) def collection: CollectionName = handle._1
  extension (handle: DocumentHandle) def key: DocumentKey = handle._2
  extension (handle: DocumentHandle)
    def unwrap: String =
      val col: CollectionName = CollectionName(handle._1).unwrap
      val key: DocumentKey = DocumentKey(handle._2).unwrap
      s"${col}/${key}"

  extension (handle: DocumentHandle) def isEmpty: Boolean = handle._1.isEmpty && handle._2.isEmpty

object DocumentKey:

  val key = "_key"

  def apply(key: String): DocumentKey = key

  extension (key: DocumentKey) def unwrap: String = key

  val empty = apply("")
  extension (key: DocumentKey) def isEmpty: Boolean = key.isEmpty

object DatabaseName:

  def apply(name: String): DatabaseName = name
  extension (name: DatabaseName) def unwrap: String = name

  given Descriptor[DatabaseName] = Descriptor.from(string)

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val system = DatabaseName("_system")

object DocumentRevision:

  val key: String = "_rev"

  def apply(value: String): DocumentRevision = value
  extension (value: DocumentRevision) def unwrap: String = value

  val empty = apply("")

object TransactionId:

  def apply(value: String): TransactionId = value
  extension (value: TransactionId) def unwrap: String = value
