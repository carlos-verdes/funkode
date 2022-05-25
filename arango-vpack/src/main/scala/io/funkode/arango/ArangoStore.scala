/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode
package arango

import avokka.arangodb.fs2._
import avokka.arangodb.models.Cursor
import avokka.arangodb.models.GraphInfo.GraphEdgeDefinition
import avokka.arangodb.protocol.ArangoRequest.PUT
import avokka.arangodb.types._
import avokka.velocypack.{VObject, VPack, VPackDecoder, VPackEncoder}
import cats.MonadThrow
import cats.effect.{Resource, Sync}
import cats.implicits.catsSyntaxApplicativeId
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import fs2.Stream
import io.funkode.rest.query.QueryResult
import org.http4s.Uri
import org.http4s.Uri.Path.Root
import org.http4s.dsl.io./
import org.http4s.headers.LinkValue
import org.http4s.implicits.http4sLiteralsSyntax


class ArangoStore[F[_]](clientR: Resource[F, Arango[F]])(implicit F: Sync[F]) extends VPackStoreDsl[F] {

  import arango.RES_REL_GRAPH
  import ArangoStore._
  import ColKey._
  import codecs.http4sCodecs._
  import rest.error._
  import rest.query._
  import rest.resource._
  import rest.syntax.all._

  def execute[R](command: (Arango[F]) => F[R]): F[R] = handleArangoErrors(clientR.use(command))

  override def store[R](uri: Uri, resource: R)(implicit S: VPackEncoder[R], D: VPackDecoder[R]): F[HttpResource[R]] =
    execute { client =>
      for {
        ColKeyOp(collectionName, keyOp) <- ColKey.fromUriOp[F](uri)
        doc = resource.encodeWithKey(keyOp)
        collection = client.db.collection(collectionName)
        _ <- collection.info().handleErrors().ifNotFound(createCollection(collection))
        stored <- collection.documents.insert(document = doc, overwrite = true, returnNew = true).handleErrors()
        newDoc <- F.fromOption(stored.`new`, runtimeError(new IllegalAccessException("Not expected error")))
        parsedDocument <- F.fromEither(D.decode(newDoc))
      } yield HttpResource(uri"/" / collectionName.repr / stored._key.repr, parsedDocument)
    }

  override def fetch[R](resourceUri: Uri)(implicit deserializer: VPackDecoder[R]): F[HttpResource[R]] =
    execute { client =>
      for {
        ColKey(collectionName, key) <- ColKey.fromUri[F](resourceUri)
        collection = client.db.collection(collectionName)
        document <- collection.document(key).read[R]().handleErrors()
        links <- getRelatedLinks(resourceUri)
      } yield HttpResource(resourceUri, document).withLinks(links)
    }

  override def linkResources(leftUri: Uri, rightUri: Uri, relType: String, attributes: Map[String, String]): F[Unit] =
    execute { client =>

      implicit val _client = client

      for {
        ColKey(leftCol, _) <- ColKey.fromUri[F](leftUri)
        ColKey(rightCol, _) <- ColKey.fromUri[F](rightUri)
        edge = client.db.collection(CollectionName(relType))
        _ <- edge.info().handleErrors().ifNotFound(createEdge(edge))
        edgeDoc = buildEdgeDoc(leftUri, rightUri, relType, attributes)
        _ <- edge.documents.insert(document = edgeDoc, overwrite = true, returnNew = true)
        edgeDefinition = GraphEdgeDefinition(relType, List(leftCol.repr), List(rightCol.repr))
        _ <- updateGraphDefinition(edgeDefinition)
      } yield ()
    }

  private def getRelatedLinks(uri: Uri): F[Vector[LinkValue]] =
    execute { client =>
      for {
        docHandle <- ColKey.fromUri[F](uri)
        query = s"FOR x, edge IN OUTBOUND '${docHandle.path}' GRAPH ${RES_REL_GRAPH} RETURN edge"
        cursor <-
            client
                .db
                .query(query)
                .batchSize(DEFAULT_BATCH_SIZE)
                .execute[LinkValue]
                .handleErrors()
                .ifNotFound(Cursor(false, None, None, false, None, Vector.empty[LinkValue]).pure[F])
      } yield cursor.result
    }

  override def getRelated[R](uri: Uri, relType: String)(implicit deserializer: VPackDecoder[R]): Stream[F, R] = {

    val query = ColKey.fromUri[F](uri).map(docHandle =>
      s"FOR x, edge IN OUTBOUND '${docHandle.path}' ${relType} RETURN x")

    Stream.eval(query).flatMap(query => queryStream[R](query))
  }

  override def query[R](
      queryStr: String,
      batchSize: Option[Long] = None)(
      implicit D: VPackDecoder[R]): F[QueryResult[R]] =
    execute { client => {
      for {
        exec <- client.db.query(queryStr).batchSize(batchSize.getOrElse(DEFAULT_BATCH_SIZE)).execute[R].handleErrors()
      } yield toQueryResult(exec)
    }}

  override def next[R](cursor: Uri)(implicit D: VPackDecoder[R]): F[QueryResult[R]] =
    execute { client => {

      for {
        ColKey(_, key) <- ColKey.fromUri(cursor)
        //_ <- if (col != DEFAULT_QUERY_COL) F.raiseError[QueryResult[R]](notFoundError(s"Query not found $cursor"))
        exec <- client.execute[Cursor[R]](PUT(client.db.name, s"/_api/cursor/${key}")).handleErrors()
      } yield toQueryResult(exec)
    }}

  override def queryStream[R](
      queryString: String,
      chunkSize: Option[Long])(
      implicit deserializer: VPackDecoder[R]): fs2.Stream[F, R] = {

    val queryResource = clientR.map((client: Arango[F]) => {
      client
          .db
          .query(queryString)
          .batchSize(chunkSize.getOrElse(DEFAULT_BATCH_SIZE))
          .stream[R]
          .handleErrorWith(t => fs2.Stream.eval(arangoErrorToRestError(t)))
    })

    Stream.resource(queryResource).flatten
  }
}

object ArangoStore {

  val DEFAULT_BATCH_SIZE = 10L
  val DEFAULT_QUERY_COL = CollectionName("queries")

  def toQueryResult[R](cursor: Cursor[R], queryCol: CollectionName = DEFAULT_QUERY_COL): QueryResult[R] =
    QueryResult(cursor.result, cursor.id.map(id => uri"/" / queryCol.repr/ id))
}

case class ColKey(collectionName: CollectionName, key: DocumentKey)
case class ColKeyOp(collectionName: CollectionName, key: Option[DocumentKey])

object ColKey {

  import io.funkode.rest.error._

  def fromUriOp[F[_]](uri: Uri)(implicit F: MonadThrow[F]): F[ColKeyOp] =
    uri.path match {
      case Root / col / key => F.pure(ColKeyOp(CollectionName(col), DocumentKey(key).some))
      case Root / col => F.pure(ColKeyOp(CollectionName(col), None))
      case _ => F.raiseError(BadRequestError(None, s"Url not supported for storage: $uri".some, None))
    }

  def fromUri[F[_]](uri: Uri)(implicit F: MonadThrow[F]): F[ColKey] =
    for {
      ColKeyOp(collectionName, keyOp) <- ColKey.fromUriOp[F](uri)
      key <- F.fromOption(keyOp, BadRequestError(None, s"Uri not supported to fetch docs: $uri".some, None))
    } yield ColKey(collectionName, key)

  implicit class ColKeyOpBodyOps[R](body: R) {

    def encodeWithKey(keyOp: Option[DocumentKey])(implicit E: VPackEncoder[R]): VPack = {

      val baseDocument = E.encode(body)

      (baseDocument, keyOp) match {
        case (doc: VObject, Some(key)) => doc.updated(DocumentKey.key, key)
        case _ => baseDocument
      }
    }
  }

  implicit class ColKeyOps(colKey: ColKey) {

    def path: String = DocumentHandle(colKey.collectionName, colKey.key).path
  }
}
