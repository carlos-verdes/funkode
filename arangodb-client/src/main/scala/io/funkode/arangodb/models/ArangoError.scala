/*
 * TODO: License goes here!
 */
package io.funkode.arangodb.models

case class ArangoError(code: Long, error: Boolean, errorMessage: String, errorNum: Long) extends Throwable:
  override def getMessage: _root_.java.lang.String =
    s"ArangoError(code: $code, num: $errorNum, message: $errorMessage)"
