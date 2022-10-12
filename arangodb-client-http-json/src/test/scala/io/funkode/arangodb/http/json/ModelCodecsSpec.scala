package io.funkode.arangodb
package http
package json

import zio.json.*
import zio.test.*
import zio.test.Assertion.*

trait ModelExamples:

  import models.*

  val serverInfo = ServerVersion("server1", "license123", "version1")
  val serverInfoWithDetails = ServerVersion("server1", "license123", "version1", Map("extra" -> "info"))

  val serverInfoJsonHeader = """
      |{
      |  "server": "server1",
      |  "license": "license123",
      |  "version": "version1"""".stripMargin
  val serverInfoJson = serverInfoJsonHeader + "\n}"
  val serverInfoWithDetailsJson = serverInfoJsonHeader +
    """,
      |  "details": {
      |    "extra": "info"
      |  }
      |}
      |""".stripMargin

object ModelCodecsSpec extends ZIOSpecDefault with ModelExamples:

  import models.*
  import codecs.given

  def assertDecode[T: JsonDecoder](json: String, expected: T) =
    println(s"trying to decode: \n$json")
    assert(json.fromJson[T])(isRight(equalTo(expected)))

  override def spec: Spec[TestEnvironment, Any] =
    suite("Model Codecs should")(
      test("decode server info") {
        assertDecode(serverInfoJson, serverInfo)
      },
      test("decode server info with details") {
        assertDecode(serverInfoWithDetailsJson, serverInfoWithDetails)
      }
    )
