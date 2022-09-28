/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import zio.config.ConfigDescriptor.*
import zio.config.magnolia.{descriptor, Descriptor}

opaque type DatabaseName = String

object DatabaseName:

  def apply(name: String): DatabaseName = name
  extension (name: DatabaseName) def unwrap: String = name

  given Descriptor[DatabaseName] = Descriptor.from(string)

  val system = DatabaseName("_system")

trait ArangoDataBase:
  protected def name: String
