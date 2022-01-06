/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode
package arango

import avokka.arangodb.fs2.Arango
import avokka.arangodb.models.GraphInfo.GraphEdgeDefinition
import avokka.arangodb.types.{CollectionName, DocumentKey, TransactionId}
import avokka.velocypack.{VObject, VPack, VPackDecoder, VPackEncoder}
import cats.MonadThrow
import cats.effect.{Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import org.http4s.Uri
import org.http4s.Uri.Path.Root
import org.http4s.dsl.io./
import org.http4s.implicits.http4sLiteralsSyntax


class ArangoStore[F[_]](clientR: Resource[F, Arango[F]])(implicit F: Sync[F]) extends VPackStoreDsl[F] {

  import ArangoStore._
  import ColKey._
  import rest.error._
  import rest.query._
  import rest.resource._

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
      } yield HttpResource(resourceUri, document)
    }

  override def linkResources(leftUri: Uri, rightUri: Uri, relType: String): F[Unit] =
    execute { client =>

      implicit val _client = client

      for {
        ColKey(leftCol, leftKey) <- ColKey.fromUri[F](leftUri)
        ColKey(rightCol, rightKey) <- ColKey.fromUri[F](rightUri)
        edge = client.db.collection(CollectionName(relType))
        _ <- edge.info().handleErrors().ifNotFound(createEdge(edge))
        edgeDoc = buildEdgeDoc(leftKey.repr + "-" + rightKey.repr, leftUri, rightUri)
        _ <- edge.documents.insert(document = edgeDoc, overwrite = true, returnNew = true)
        edgeDefinition = GraphEdgeDefinition(relType, List(leftCol.repr), List(rightCol.repr))
        _ <- updateGraphDefinition(edgeDefinition)
      } yield ()
    }

  override def query[R](
      query: String,
      batchSize: Option[Long],
      cursor: Option[String])(
      implicit deserializer: VPackDecoder[R]): F[QueryResult[R]] =

    execute { client => {

      val arangoQuery = client.db.query(query).batchSize(batchSize.getOrElse(DEFAULT_BATCH_SIZE))
      val queryWithCursor = cursor.map(c => arangoQuery.transaction(TransactionId(c))).getOrElse(arangoQuery)

      for {
        queryResult <- queryWithCursor.execute[R].handleErrors()
      } yield QueryResult(queryResult.result, queryResult.id)
    }
  }
}

object ArangoStore {

  val DEFAULT_BATCH_SIZE = 10L
}

case class ColKeyOp(collectionName: CollectionName, key: Option[DocumentKey])
case class ColKey(collectionName: CollectionName, key: DocumentKey)

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

  implicit class ColKeyOpOps[R](body: R) {


    def encodeWithKey(keyOp: Option[DocumentKey])(implicit E: VPackEncoder[R]): VPack = {

      val baseDocument = E.encode(body)

      (baseDocument, keyOp) match {
        case (doc: VObject, Some(key)) => doc.updated(DocumentKey.key, key)
        case _ => baseDocument
      }
    }
  }
}
