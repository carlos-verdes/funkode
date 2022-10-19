package io.funkode.arangodb.models

import io.funkode.arangodb.models
import io.funkode.arangodb.protocol.ArangoMessage
import io.funkode.arangodb.protocol.ArangoMessage.error
import io.funkode.arangodb.protocol.ArangoMessageSpec.{suite, test}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

import models.*

trait ModelExamples:

  val col = CollectionName("col123")
  val key = DocumentKey("keyabc")
  val documentHandle = DocumentHandle(col, key)
  val documentUri = "col123/keyabc"

object OpaqueTypesSpec extends ZIOSpecDefault with ModelExamples:

  import models.*

  override def spec: Spec[TestEnvironment, Any] =
    suite("Opaque types should")(
      test("Parse DocumentHandle from a uri string") {
        assertTrue(DocumentHandle.parse(documentUri) == Some(documentHandle)) &&
        assertTrue(DocumentHandle.parse("wrong/uri/handle").isEmpty)
      },
      test("Provide a uri string as representation of DocumentHandle") {
        assertTrue(documentHandle.unwrap == documentUri) &&
        assertTrue(documentHandle.collection == col) &&
        assertTrue(documentHandle.key == key)
      }
    )
