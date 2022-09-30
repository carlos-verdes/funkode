/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package protocol

import MessageType.*

// only needed for VelocyStream
enum MessageType(value: Int, show: String):
  case Request extends MessageType(1, REQUEST)
  case ResponseFinal extends MessageType(2, RESPONSE_FINAL)
  case ResponseChunk extends MessageType(3, RESPONSE_CHUNK)
  case Authentication extends MessageType(1000, AUTHENTICATION)

object MessageType:

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val REQUEST = "request"

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val RESPONSE_FINAL = "response-final"

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val RESPONSE_CHUNK = "response-chunk"

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val AUTHENTICATION = "authentication"
