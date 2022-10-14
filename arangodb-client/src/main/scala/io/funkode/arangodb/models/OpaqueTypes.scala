package io.funkode.arangodb.models

import zio.config.ConfigDescriptor.string
import zio.config.magnolia.Descriptor

opaque type CollectionName = String
opaque type DocumentKey = String
opaque type DatabaseName = String

object CollectionName:

  def apply(name: String): CollectionName = name
  extension (name: CollectionName) def unwrap: String = name

  import zio.config.ConfigDescriptor.string
  import zio.config.magnolia.Descriptor

  given Descriptor[CollectionName] = Descriptor.from(string)

object DocumentKey:

  val key = "_key"

  def apply(key: String): DocumentKey = key

  extension (key: DocumentKey) def unwrap: String = key

  val empty = DocumentKey("")
  extension (key: DocumentKey) def isEmpty: Boolean = key.isEmpty


object DatabaseName:

  def apply(name: String): DatabaseName = name
  extension (name: DatabaseName) def unwrap: String = name

  given Descriptor[DatabaseName] = Descriptor.from(string)

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val system = DatabaseName("_system")
