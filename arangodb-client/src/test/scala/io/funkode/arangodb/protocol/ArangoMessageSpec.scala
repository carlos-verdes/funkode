package io.funkode.arangodb
package protocol

import zio.test.*
import zio.test.Assertion.*

trait ArangoMessageExamples:

  import models.*
  import ArangoMessage.*

  val customErrorNoMessage = ArangoError(400, true, "", -1)
  val customErrorWithMessage = ArangoError(404, true, "not found", -1)

object ArangoMessageSpec extends ZIOSpecDefault with ArangoMessageExamples:

  import ArangoMessage.*

  override def spec: Spec[TestEnvironment, Any] =
    suite("ArangoMessage should")(
      test("create custom error without message") {
        assertTrue(error(400) == customErrorNoMessage)
      },
      test("create custom error with message") {
        assertTrue(error(404, "not found") == customErrorWithMessage)
      }
    )
