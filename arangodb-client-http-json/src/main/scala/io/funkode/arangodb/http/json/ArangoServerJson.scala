package io.funkode.arangodb
package http
package json

import zio.*
import zio.json.*

object ArangoServerJson:

  import models.*
  import protocol.*
  import codecs.given_JsonCodec_ServerVersion

  def version(details: Boolean = false): JRAIO[ServerVersion] =
    ArangoServer.version(details)
