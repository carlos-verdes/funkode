/*
 * TODO: License goes here!
 */
package io.funkode.resource
package outbound

import io.lemonlabs.uri.Urn
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.schema.*
import zio.stream.*

import model.*
import Resource.*

type ResourceApiCall[R] = IO[ResourceError, R]
type ResourceStream[R] = Stream[ResourceError, R]

case class StoreModel(graphs: List[GraphModel])
case class GraphModel(name: String, collections: List[CollectionModel])
case class CollectionModel(
    name: String,
    relationships: List[RelationshipModel] = List.empty
)
case class RelationshipModel(rel: String, targetCollection: String)

trait ResourceStore[Encoder[_], Decoder[_], Document]:

  type DocResource = Resource[Encoder, Decoder, Document]

  def initStore(storeModel: StoreModel): ResourceApiCall[Unit]

  def fetch(urn: Urn): ResourceApiCall[DocResource]
  def store(urn: Urn, document: Document): ResourceApiCall[DocResource]
  def store[R: Encoder](urn: Urn, r: R): ResourceApiCall[DocResource]

  def store[R: Encoder: Identifiable](r: R): ResourceApiCall[DocResource] = store(r.urn, r)

  // def link(leftUrn: Urn, relType: String, rightUrn: Urn): ResourceApiCall[Unit]
  // def fetchRel(urn: Urn, relType: String): ResourceStream[DocResource]

trait JsonStore extends ResourceStore[JsonEncoder, JsonDecoder, Json]:
  override type DocResource = JsonResource

object JsonStore:

  type WithJsonStore[R] = ZIO[JsonStore, ResourceError, R]

  def withStore[R](f: JsonStore => WithJsonStore[R]) = ZIO.service[JsonStore].flatMap(f)

  def initStore(storeModel: StoreModel): WithJsonStore[Unit] = withStore(_.initStore(storeModel))

  def fetch(urn: Urn): WithJsonStore[JsonResource] = withStore(_.fetch(urn))

  def store[R: JsonEncoder](urn: Urn, r: R): WithJsonStore[JsonResource] =
    withStore(_.store(urn, r))

  def store[R: JsonEncoder: Identifiable](r: R): WithJsonStore[JsonResource] = store(r.urn, r)

  extension (resourceIO: WithJsonStore[JsonResource])
    def deserialize[R: JsonDecoder] = resourceIO.flatMap(_.deserialize[R])
