/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package http
package json

import zio.http.*
import zio.http.model.*
import zio.test.*

import models.*
import protocol.*
import ArangoMessage.*
import codecs.given
import Conversions.given
import Extensions.*

trait MessageRequestReponseExamples:

  val httpHeaders =
    Headers.empty ++ Headers.server("ArangoDB") ++ Headers("X-Content-Type-Options", "nosniff")
  val arangoHeaders = Seq("server" -> "ArangoDB", "X-Content-Type-Options" -> "nosniff").toMap

  val noHttpHeaders = Headers.empty
  val noArangoHeaders = Map.empty[String, String]

  val loginToken = "eyJhbGciO...f5jDuc"
  val loginResponseOkJson = s"{\"jwt\":\"$loginToken\"}"
  val httpResponseLoginOk = Response(Status.Ok, httpHeaders, Body.fromString(loginResponseOkJson))

  val credentials = Token(loginToken)
  val arangoResponseLoginOk = ArangoMessage(responseFinal(200, arangoHeaders), credentials)

  val queryParams = QueryParams("a" -> " 1", "b" -> "2")
  val arangoParams = Seq("a" -> " 1", "b" -> "2").toMap

  val noQueryParams = QueryParams.empty
  val noArangoParams = Map.empty[String, String]

  val rootLemonPath = io.lemonlabs.uri.UrlPath.slash
  val emptyLemonPath = io.lemonlabs.uri.UrlPath.empty
  val lemonPath = io.lemonlabs.uri.UrlPath.fromRaw("/some/path")
  val rootLessLemonPath = io.lemonlabs.uri.UrlPath.fromRaw("some/path")

  val httpRootPath = Path.root
  val httpEmptyPath = Path.empty
  val httpPath = Path.decode("/some/path")
  val httpRootLessPath = Path.decode("some/path")

object ArangoClientSpec extends ZIOSpecDefault with MessageRequestReponseExamples:

  def validateConversion[From, To](from: From, to: To)(using Conversion[From, To]) =
    val converted: To = from
    assertTrue(converted == to)

  def validateRequestToTypeHttpMethodConversion = validateConversion[RequestType, Method]
  def validateHttpResponseToArangoHeaderConversion = validateConversion[Response, ArangoMessage.Header]
  def validateArangoHeaderMetaToHttpHeaderConversion = validateConversion[Map[String, String], Headers]
  def validateArangoHeaderParamsToHttpParamsConversion = validateConversion[Map[String, String], QueryParams]
  def validateLemonPathtoZioPathConversion = validateConversion[io.lemonlabs.uri.UrlPath, zio.http.Path]

  override def spec: Spec[TestEnvironment, Any] =
    suite("ArangoClientHttpJson should")(
      test("convert from RequestType to HttpMethod") {
        validateRequestToTypeHttpMethodConversion(RequestType.DELETE, Method.DELETE) &&
        validateRequestToTypeHttpMethodConversion(RequestType.GET, Method.GET) &&
        validateRequestToTypeHttpMethodConversion(RequestType.POST, Method.POST) &&
        validateRequestToTypeHttpMethodConversion(RequestType.PUT, Method.PUT) &&
        validateRequestToTypeHttpMethodConversion(RequestType.HEAD, Method.HEAD) &&
        validateRequestToTypeHttpMethodConversion(RequestType.PATCH, Method.PATCH) &&
        validateRequestToTypeHttpMethodConversion(RequestType.OPTIONS, Method.OPTIONS)
      },
      test("convert from http response to Arango message header") {
        validateHttpResponseToArangoHeaderConversion(httpResponseLoginOk, arangoResponseLoginOk.header)
      },
      test("convert from Arango meta to Http headers") {
        validateArangoHeaderMetaToHttpHeaderConversion(arangoHeaders, httpHeaders) &&
        validateArangoHeaderMetaToHttpHeaderConversion(noArangoHeaders, noHttpHeaders)
      },
      test("convert from Arango params to URL query params") {
        validateArangoHeaderParamsToHttpParamsConversion(arangoParams, queryParams) &&
        validateArangoHeaderParamsToHttpParamsConversion(noArangoParams, noQueryParams)
      },
      test("convert lemon Path into zio Path") {
        validateLemonPathtoZioPathConversion(emptyLemonPath, httpEmptyPath) &&
        validateLemonPathtoZioPathConversion(lemonPath, httpPath) &&
        validateLemonPathtoZioPathConversion(rootLessLemonPath, httpRootLessPath) &&
        validateLemonPathtoZioPathConversion(rootLemonPath, httpRootPath)
      },
      test("create a json body") {
        for jsonBodyFromCaseClass <- credentials.jsonBody.asString
        yield assertTrue(jsonBodyFromCaseClass == loginResponseOkJson)
      }
    )
