package io.funkode.arangodb

import io.lemonlabs.uri.*
import io.lemonlabs.uri.typesafe.dsl.*
import zio.*
import models.*
import protocol.*

type AIO[A] = IO[ArangoError, A]
type RAIO[Encoder[_], Decoder[_], O] = ZIO[ArangoClient[Encoder, Decoder], ArangoError, O]

val Api = "_api"
val Collection = "collection"
val Cursor = "cursor"
val Database = "database"
val DocumentString = "document"
val Db = "_db"
val Gharial = "gharial"
val Index = "index"
val Transaction = "transaction"
val VersionString = "version"

val ApiVersionPath = AbsolutePath.fromParts(Api, VersionString)
val ApiDatabaseManagementPath = AbsolutePath.fromParts(Api, Database)
def apiDatabasePrefixPath(databaseName: DatabaseName) = AbsolutePath.fromParts(Db, databaseName.unwrap)
val ApiCollectionPath = AbsolutePath.fromParts(Api, Collection)
val ApiDocumentPath = AbsolutePath.fromParts(Api, DocumentString)
val ApiIndexPath = AbsolutePath.fromParts(Api, Index)
val ApiCursorPath = AbsolutePath.fromParts(Api, Cursor)
val ApiTransactionPath = AbsolutePath.fromParts(Api, Transaction)
val ApiGharialPath = AbsolutePath.fromParts(Api, Gharial)
