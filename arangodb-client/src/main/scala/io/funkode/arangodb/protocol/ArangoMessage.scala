package io.funkode.arangodb
package protocol

import scala.collection.immutable.Map

import io.lemonlabs.uri.UrlPath
import zio.prelude.Covariant

case class ArangoMessage[+T](header: ArangoMessage.Header, body: T)

object ArangoMessage:

  import models.*

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val Plain = "plain"

  enum Header(version: ArangoVersion, `type`: MessageType):
    case Request(
        version: ArangoVersion = ArangoVersion.Current,
        database: DatabaseName = DatabaseName.system,
        requestType: RequestType,
        request: UrlPath,
        parameters: Map[String, String] = Map.empty,
        meta: Map[String, String] = Map.empty
    ) extends Header(version, MessageType.Request)

    case Response(
        version: ArangoVersion,
        `type`: MessageType,
        responseCode: Long,
        meta: Map[String, String] = Map.empty
    ) extends Header(version, `type`)

    case Authentication(
        encryption: String,
        credentials: UserPassword | Token
    ) extends Header(ArangoVersion.Current, MessageType.Authentication)

  def DELETE(
      database: DatabaseName,
      request: UrlPath,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): Header = Header.Request(ArangoVersion.Current, database, RequestType.DELETE, request, parameters, meta)

  def GET(
      database: DatabaseName,
      request: UrlPath,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): Header = Header.Request(ArangoVersion.Current, database, RequestType.GET, request, parameters, meta)

  def POST(
      database: DatabaseName,
      request: UrlPath,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): Header = Header.Request(ArangoVersion.Current, database, RequestType.POST, request, parameters, meta)

  def PUT(
      database: DatabaseName,
      request: UrlPath,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): Header = Header.Request(ArangoVersion.Current, database, RequestType.PUT, request, parameters, meta)

  def HEAD[T](
      database: DatabaseName,
      request: UrlPath,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): Header = Header.Request(ArangoVersion.Current, database, RequestType.HEAD, request, parameters, meta)

  def PATCH(
      database: DatabaseName,
      request: UrlPath,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): Header = Header.Request(ArangoVersion.Current, database, RequestType.PATCH, request, parameters, meta)

  def OPTIONS(
      database: DatabaseName,
      request: UrlPath,
      parameters: Map[String, String] = Map.empty,
      meta: Map[String, String] = Map.empty
  ): Header = Header.Request(ArangoVersion.Current, database, RequestType.OPTIONS, request, parameters, meta)

  def responseFinal(code: Long, meta: Map[String, String] = Map.empty): Header.Response =
    Header.Response(ArangoVersion.Current, MessageType.ResponseFinal, code, meta)

  def error(code: Long, msg: String = ""): ArangoError = ArangoError(code, true, msg, -1)

  def login(user: String, password: String): Header.Authentication =
    Header.Authentication(Plain, UserPassword(user, password))

  given Covariant[ArangoMessage] = new Covariant[ArangoMessage]:
    override def map[A, B](f: A => B): ArangoMessage[A] => ArangoMessage[B] =
      (a: ArangoMessage[A]) => a.copy(body = f(a.body))
