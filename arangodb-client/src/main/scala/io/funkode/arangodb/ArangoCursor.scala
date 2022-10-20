/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

import protocol.*
import models.*

trait ArangoCursor[T, Decoder[_]]:
  def header: ArangoMessage.Header
  def body: Cursor[T]
  def next(): AIO[ArangoCursor[T, Decoder]]
  def delete(): AIO[DeleteResult]
