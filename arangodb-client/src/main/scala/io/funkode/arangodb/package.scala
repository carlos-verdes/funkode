package io.funkode.arangodb

import io.lemonlabs.uri.UrlPath
import io.lemonlabs.uri.typesafe.dsl.*
import zio.*
import models.*
import protocol.*

type AIO[A] = IO[ArangoError, A]
type RAIO[Encoder[_], Decoder[_], O] = ZIO[ArangoClient[Encoder, Decoder], ArangoError, O]


val ApiVersion = UrlPath.parse("/_api/version")
val ApiDatabase = UrlPath.parse("/_api/database")
val ApiCollection = UrlPath.parse("/_api/collection")
val ApiDocument = UrlPath.parse("/_api/document")
val ApiIndex = UrlPath.parse("/_api/index")
val ApiCursor = UrlPath.parse("/_api/cursor")
val ApiTransaction = UrlPath.parse("/_api/transaction")
val ApiGharial = UrlPath.parse("/_api/gharial")
