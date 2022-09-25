/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import io.funkode.arangodb

opaque type DatabaseName = String

object DatabaseName:

  def apply(name: String): DatabaseName = name
  extension (name: DatabaseName) def unwrap: String = name

  val system = DatabaseName("_system")

trait ArangoDataBase:
  protected def name: String
