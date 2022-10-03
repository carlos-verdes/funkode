/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package http
package json

import zio.json.*
import zio.json.internal.*

object ModelCodecs:

  import models.*

  given JsonCodec[ArangoError] = DeriveJsonCodec.gen[ArangoError]
  given JsonCodec[ServerVersion] = DeriveJsonCodec.gen[ServerVersion]
  given JsonCodec[Token] = DeriveJsonCodec.gen[Token]
  given JsonCodec[UserPassword] = DeriveJsonCodec.gen[UserPassword]
