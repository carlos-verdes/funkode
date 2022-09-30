package io.funkode.arangodb.protocol

opaque type Version = Int

object Version:

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val CURRENT: Version = 1

  def apply(version: Int): Version = 1
  extension (version: Version) def unwrap: Int = version
