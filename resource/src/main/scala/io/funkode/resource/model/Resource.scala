/*
 * TODO: License goes here!
 */
package io.funkode.resource.model

import scala.quoted.{Expr, Quotes, Type}

import io.lemonlabs.uri.Urn
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.schema.*
import zio.schema.meta.MetaSchema
import zio.stream.*

opaque type Etag = String
object Etag:
  def apply(etag: String): Etag = etag
  extension (etag: Etag) def unwrap: String = etag

enum ResourceError(msg: String, cause: Option[Throwable] = None) extends Throwable(msg, cause.orNull):
  case NotFoundError(urn: Option[Urn])
      extends ResourceError(s"""Resource ${urn.map("with id " + _).getOrElse("")} not found""")
  case SerializationError(msg: String, cause: Option[Throwable] = None) extends ResourceError(msg, cause)
  case FormatError(msg: String, cause: Option[Throwable] = None)
      extends ResourceError(s"Format not supported: $msg", cause)
  case UnderlinedError(cause: Throwable) extends ResourceError("Non controlled error", Some(cause))

case class ResourceLink(urn: Urn, rel: String, attributes: Map[String, String] = Map.empty)
type ResourceLinks = Map[String, ResourceLink]

trait Resource[Encoder[_], Decoder[_], Body]:

  def id: Urn

  def body: Body
  def deserialize[R: Decoder]: IO[ResourceError, R]

  def etag: Option[Etag]

  def links: ResourceLinks = Map.empty

type JsonResource = Resource[JsonEncoder, JsonDecoder, Json]

object Resource:

  trait Identifiable[R]:
    extension (r: R)
      def urn: Urn
      def withId(urn: Urn): R
