package io.funkode.auth

import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.*

case class JwtConfiguration(signingKey: String)

object JwtConfiguration:

  import ConfigDescriptor.nested

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val DefaultPath = "jwt"

  val jwtConfigDescriptor = descriptor[JwtConfiguration].mapKey(toKebabCase)

  def fromPath(path: String) = TypesafeConfig.fromResourcePath(nested(path)(jwtConfigDescriptor))

  val default = fromPath(DefaultPath)
