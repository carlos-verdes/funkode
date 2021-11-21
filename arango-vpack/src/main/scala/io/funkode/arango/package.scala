/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode

import avokka.arangodb.{ArangoCollection, ArangoGraph}
import avokka.arangodb.models.CollectionCreate.KeyOptions
import avokka.arangodb.models.GraphInfo.{GraphEdgeDefinition, GraphRepresentation}
import avokka.arangodb.models.{CollectionInfo, CollectionType}
import avokka.arangodb.protocol.{ArangoClient, ArangoError, ArangoResponse}
import avokka.velocypack.{VObject, VPack, VPackDecoder, VPackEncoder, VPackError}
import cats.MonadThrow
import cats.implicits.toFlatMapOps
import cats.syntax.functor._
import cats.syntax.option._
import org.http4s.{Status, Uri}
import org.log4s.getLogger

package object arango {

  import rest.error._
  import rest.store._

  type VPackStoreDsl[F[_]] = HttpStoreDsl[F, VPackEncoder, VPackDecoder]

  val RESOURCE_RELS_GRAPH = "resource-rels"

  val logger = getLogger

  def createCollection[F[_] : MonadThrow](
      collection: ArangoCollection[F],
      collectionType: CollectionType = CollectionType.Document): F[CollectionInfo] =
    collection
        .create(_.copy(keyOptions = KeyOptions(allowUserKeys = true.some).some, `type` = collectionType))
        .handleErrors()
        .ifConflict(collection.info().handleErrors())

  def getCreateGraph[F[_] : MonadThrow, A](graph: ArangoGraph[F]): F[GraphRepresentation] = {
    graph.info().handleErrors().ifNotFound(graph.create().handleErrors()).map(_.graph)
  }

  def updateGraphDefinition[F[_]](
      ed: GraphEdgeDefinition)(
      implicit client: ArangoClient[F],
      F: MonadThrow[F]): F[Unit] = {

    val graph = ArangoGraph(client.db.name, RESOURCE_RELS_GRAPH)

    for {
      edgeDefinitions <- getCreateGraph(graph).map(_.edgeDefinitions)
      _ <- {
        edgeDefinitions.filter(_.collection == ed.collection).headOption match {
          case Some(current) =>
            if ((current.from.intersect(ed.from) != ed.from) || (current.to.intersect(ed.to) != ed.to)) {
              logger.info(s"Update edge definition, adding: $ed, \ncurrent: $current")
              val from = ed.from.concat(current.from)
              val to = ed.to.concat(current.to)
              graph.replaceEdgeDefinition(GraphEdgeDefinition(ed.collection, from, to))
            } else {
              logger.info(s"all settle for edge definitions")
              F.pure(())
            }
          case None =>
            logger.info(s"Creating edge definition first time: $ed")
            graph.addEdgeDefinition(ed)
        }

      }
    } yield ()
  }

  def createEdge[F[_] : MonadThrow](collection: ArangoCollection[F]): F[CollectionInfo] =
    createCollection(collection, CollectionType.Edge)

  def buildEdgeDoc(key: String, leftUri: Uri, rightUri: Uri): VPack =
    VObject
        .empty
        .updated("_key", key)
        .updated("_from", leftUri.path.toString().substring(1))
        .updated("_to", rightUri.path.toString().substring(1))

  def handleArangoErrors[F[_], R](effect: F[R])(implicit F: MonadThrow[F]): F[R] = {
    effect.flatMap(_ match {
      case ArangoResponse(_, ar: ArangoError) => arangoErrorToRestError(ar)
      case _ => effect
    })
  }

  implicit class ArangoResponseOps[F[_], R](arangoResponse: F[ArangoResponse[R]])(implicit F: MonadThrow[F]) {

    def handleErrors(): F[R] = F.handleErrorWith(arangoResponse.map(_.body))(arangoErrorToRestError[F, R])
  }

  def arangoErrorToRestError[F[_], R](arangoError: Throwable)(implicit F: MonadThrow[F]): F[R] = {
    F.raiseError(
      arangoError match {
        case ArangoError.Response(ArangoResponse.Header(_, _, Status.BadRequest.code, _), _) =>
          BadRequestError(None, None, arangoError.some)
        case ArangoError.Response(ArangoResponse.Header(_, _, Status.NotFound.code, _), _) =>
          NotFoundError(None, None, arangoError.some)
        case ArangoError.Response(ArangoResponse.Header(_, _, Status.Conflict.code, _), _) =>
          ConflictError(None, None, arangoError.some)
        case vPackError: VPackError =>
          BadRequestError(None, s"Error coding/decoding VPack".some, vPackError.some)
      })
  }
}
