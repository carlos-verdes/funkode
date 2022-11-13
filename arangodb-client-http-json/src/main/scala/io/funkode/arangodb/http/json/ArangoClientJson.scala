/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package http
package json

import java.net.{MalformedURLException, URISyntaxException}

import io.lemonlabs.uri.*
import io.netty.handler.codec.http.HttpHeaderNames
import zio.*
import zio.http.*
import zio.http.model.*
import zio.http.model.Headers.BearerSchemeName
import zio.http.model.Status.*
import zio.json.*
import zio.prelude.*

import models.*
import protocol.*

trait ArangoClientJson extends ArangoClient[JsonEncoder, JsonDecoder]

object ArangoClientJson:

  def apply(
      config: ArangoConfiguration,
      httpClient: Client,
      token: Option[Token] = None
  ): ArangoClientJson = new ArangoClientJson:

    import Constants.*
    import Conversions.given
    import Extensions.*
    import codecs.given

    private val BaseUrl = URL(!!).setScheme(Scheme.HTTP).setHost(config.host).setPort(config.port)

    private val headers =
      token.map(_.jwt).map(Headers.bearerAuthorizationHeader).getOrElse(Headers.empty)

    def parseJson[O: JsonDecoder](bodyString: String): IO[ArangoError, O] =
      ZIO.fromEither(bodyString.fromJson[O].leftMap(ArangoMessage.error(BadRequest.code, _)))

    def head(header: ArangoMessage.Header): AIO[ArangoMessage.Header] =
      for response <- httpClient.request(header.emptyRequest(BaseUrl, headers)).handleErrors
      yield response

    def get[O: JsonDecoder](header: ArangoMessage.Header): AIO[ArangoMessage[O]] =
      for
        response <- httpClient.request(header.emptyRequest(BaseUrl, headers)).handleErrors
        body <- parseResponseBody(response)
      yield ArangoMessage(response, body)

    def command[I: JsonEncoder, O: JsonDecoder](message: ArangoMessage[I]): AIO[ArangoMessage[O]] =
      for
        response <- httpClient.request(message.httpRequest(BaseUrl, headers)).handleErrors
        body <- parseResponseBody(response)
      yield ArangoMessage(response, body)

    def login(username: String, password: String): AIO[Token] =
      for token <- getBody[Token](ArangoMessage.login(username, password))
      yield token

    private def parseResponseBody[O: JsonDecoder](response: Response): AIO[O] =
      for
        bodyString <- response.body.asString.handleErrors
        // _ <- ZIO.succeed(println(s"Parsing response body: \n$bodyString"))
        body <-
          if response.status.isError
          then parseJson[ArangoError](bodyString).flatMap(r => ZIO.fail(r))
          else parseJson(bodyString)
      yield body

    def currentDatabase: DatabaseName = (config.database)

  // def server: ArangoServer[F]
  // def database(name: DatabaseName): ArangoDatabase[F]
  // def system: ArangoDatabase[F]
  // def db: ArangoDatabase[F]

  def initArangoClient(config: ArangoConfiguration, httpClient: Client) =
    for token <- ArangoClientJson(config, httpClient).login(config.username, config.password)
    yield ArangoClientJson(config, httpClient, Some(token))

  val live: ZLayer[ArangoConfiguration & Client, ArangoError, ArangoClientJson] =
    ZLayer(for
      config <- ZIO.service[ArangoConfiguration]
      httpClient <- ZIO.service[Client]
      arangoClient <- initArangoClient(config, httpClient)
    yield arangoClient)

object Constants:

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val Open = "_open"
  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val Auth = "auth"
  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val Api = "_api"
  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val Job = "job"

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val EmptyString = ""

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val RuntimeError = "Runtime error "

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val ArangoAsyncId = "x-arango-async-id"

  val ApiPath = zio.http.Path.root / Api
  val LoginPath = zio.http.Path.root / Open / Auth

  def asyncResponsePath(jobId: String) = ApiPath / Job / jobId

object Conversions:

  import Constants.*
  import Extensions.jsonBody

  given Conversion[RequestType, Method] = _ match
    case RequestType.DELETE  => Method.DELETE
    case RequestType.GET     => Method.GET
    case RequestType.POST    => Method.POST
    case RequestType.PUT     => Method.PUT
    case RequestType.HEAD    => Method.HEAD
    case RequestType.PATCH   => Method.PATCH
    case RequestType.OPTIONS => Method.OPTIONS

  given Conversion[Response, ArangoMessage.Header] = resp =>
    ArangoMessage.responseFinal(
      resp.status.code,
      resp.headers.iterator.map(h => (h.key.toString, h.value.toString)).toMap
    )

  given Conversion[Vector[String], zio.http.Path] = parts =>
    zio.http.Path(parts.map(zio.http.Path.Segment.apply))

  given Conversion[io.lemonlabs.uri.UrlPath, zio.http.Path] = _ match
    case path: AbsoluteOrEmptyPath =>
      path match
        case EmptyPath           => zio.http.Path.empty
        case AbsolutePath(parts) => zio.http.Path.root ++ parts
    case RootlessPath(parts) => parts

  given Conversion[Map[String, String], Headers] =
    _.map((k, v) => Headers(k, v)).fold(Headers.empty)(_ ++ _)

  given Conversion[Map[String, String], QueryParams] = params =>
    if params.isEmpty then QueryParams.empty
    else
      val values = params.view.iterator.toList
      QueryParams(values.head, values.tail*)

object Extensions:

  import Constants.*
  import Conversions.given
  import codecs.given

  extension [T: JsonEncoder](t: T) def jsonBody = Body.fromString(t.toJson)

  def requestWithBody(body: Body, headers: Headers, method: Method, url: URL): Request =
    Request(body, headers, method, url, Version.Http_1_1, Option.empty)

  def requestHeader(headers: Headers, method: Method, url: URL): Request =
    Request(Body.empty, headers, method, url, Version.Http_1_1, Option.empty)

  extension (header: ArangoMessage.Header)
    def emptyRequest(baseUrl: URL, extraHeaders: Headers = Headers.empty) = header match
      case ArangoMessage.Header.Request(_, database, requestType, requestPath, parameters, meta) =>
        val requestUrl = baseUrl.setPath(apiDatabasePrefixPath(database).addParts(requestPath.parts))
        // println(s"$requestType ${requestUrl}")
        val headers: Headers = meta
        requestHeader(headers ++ extraHeaders, requestType, requestUrl.setQueryParams(parameters))

      // support for async responses https://www.arangodb.com/docs/stable/http/async-results-management.html#managing-async-results-via-http
      case ArangoMessage.Header.Response(_, _, _, meta) =>
        val headers: Headers = meta
        requestHeader(
          headers ++ extraHeaders,
          Method.PUT,
          baseUrl.setPath(asyncResponsePath(meta.get(ArangoAsyncId).getOrElse(EmptyString)))
        )

      case ArangoMessage.Header.Authentication(_, credentials) =>
        val body = credentials match
          case userPassword: UserPassword => Body.fromString(userPassword.toJson)
          case token: Token               => Body.fromString(token.toJson)
        requestWithBody(body, Map.empty, Method.POST, baseUrl.setPath(LoginPath))

  extension [T: JsonEncoder](arangoMessage: ArangoMessage[T])
    def httpRequest(baseUrl: URL, extraHeaders: Headers = Headers.empty) =
      val header = arangoMessage.header.emptyRequest(baseUrl, extraHeaders)
      header.copy(body = arangoMessage.body.jsonBody)

  extension (s: String | Null) def getOrEmpty: String = if s != null then s else ""

  extension [A](call: IO[Throwable, A])
    def handleErrors: IO[ArangoError, A] =
      call.catchAll {
        case e: MalformedURLException =>
          ZIO.fail(ArangoMessage.error(Status.BadRequest.code, e.getMessage.getOrEmpty))
        case t: Throwable =>
          ZIO.fail(
            ArangoMessage.error(Status.InternalServerError.code, RuntimeError + t.getMessage.getOrEmpty)
          )
      }
