/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package protocol

import zio.*

case class ArangoRequest[T](
    database: DatabaseName = DatabaseName.system,
    requestType: RequestType,
    request: String,
    parameters: Map[String, String] = Map.empty,
    meta: Map[String, String] = Map.empty,
    body: Option[T] = None
) extends ArangoMessage[T](Version.CURRENT, MessageType.Request, meta, body)

object ArangoRequest:

  extension [T](req: ArangoRequest[?])
    def withBody(newBody: T): ArangoRequest[T] =
      req.copy(body = Some(newBody))

  def DELETE(
      database: DatabaseName,
      request: String,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): ArangoRequest[Nothing] =
    ArangoRequest(database, RequestType.DELETE, request, parameters, meta)

  def GET(
      database: DatabaseName,
      request: String,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): ArangoRequest[Nothing] =
    ArangoRequest(database, RequestType.GET, request, parameters, meta)

  def POST[T](
      database: DatabaseName,
      request: String,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): ArangoRequest[T] =
    ArangoRequest(database, RequestType.POST, request, parameters, meta)

  def PUT[T](
      database: DatabaseName,
      request: String,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): ArangoRequest[T] =
    ArangoRequest(database, RequestType.PUT, request, parameters, meta)

  def HEAD[T](
      database: DatabaseName,
      request: String,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): ArangoRequest[T] =
    ArangoRequest(database, RequestType.HEAD, request, parameters)

  def PATCH[T](
      database: DatabaseName,
      request: String,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): ArangoRequest[T] =
    ArangoRequest(database, RequestType.PATCH, request, parameters)

  def OPTIONS[T](
      database: DatabaseName,
      request: String,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): ArangoRequest[T] =
    ArangoRequest(database, RequestType.OPTIONS, request, parameters)
