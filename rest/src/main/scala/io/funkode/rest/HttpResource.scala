/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.rest

import cats.implicits.catsSyntaxOptionId
import cats.{Functor, MonadThrow}
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.funkode.rest.error.{NotFoundError, RestError}
import org.http4s._
import org.http4s.circe.{decodeUri, encodeUri}
import org.http4s.headers.{Link, LinkValue, Location}
import simulacrum.typeclass


object resource {

  val LINKS = "links"
  val REL_SELF = "self"

  case class ResourceLink(uri: Uri, rel: String, attributes: Map[String, String] = Map.empty)
  type ResourceLinks = Map[String, ResourceLink]

  case class HttpResource[R](uri: Uri, body: R, links: ResourceLinks = Map.empty)
  type HttpResources[R] = Vector[HttpResource[R]]

  @typeclass
  trait ToResource[R] {

    def uri(r: R): Uri
    def asResource(r: R): HttpResource[R] = HttpResource(uri(r), r)
  }

  object ResourceLink {

    implicit def fromTuple(t: (String, Uri)): ResourceLink = ResourceLink(t._2, t._1)
  }

  object ResourceLinks {

    def apply(resourceLink: ResourceLink): ResourceLinks = Map(resourceLink.rel -> resourceLink)
    def apply(resourceLinks: Vector[ResourceLink]): ResourceLinks = resourceLinks.map(rl => (rl.rel -> rl)).toMap
  }

  object HttpResource {

    def link(uri: Uri, rel: String): ResourceLink = ResourceLink(uri, rel)
    def selfLink(uri: Uri): ResourceLink = ResourceLink(uri, REL_SELF)

    def apply[R](uri: Uri, body: R): HttpResource[R] = HttpResource(uri, body, ResourceLinks(selfLink(uri)))

    implicit def functorInstance: Functor[HttpResource] =  new Functor[HttpResource] {
      override def map[A, B](resource: HttpResource[A])(f: A => B): HttpResource[B] =
        resource.copy(body = f(resource.body))
    }

    implicit val encodeResourceLinks: Encoder[ResourceLinks] = (link: ResourceLinks) =>
      Json.obj(LINKS -> Json.fromFields(link.values.map(link => link.rel -> Encoder[Uri].apply(link.uri))))

    implicit val decodeResourceLinks: Decoder[ResourceLinks] = (c: HCursor) =>
      c.downField(LINKS).as[Map[String, Uri]].map(_.map(t => t._1 -> ResourceLink.fromTuple(t)))
  }

  trait HttpResourceSyntax {

    implicit class HttpResourceOps[R](res: HttpResource[R]) {

      private def relNotFoundErr(r: String): RestError = NotFoundError(r.some, s"Rel $r not found for $res.".some, None)

      def selfLink: ResourceLink = HttpResource.selfLink(res.uri)

      def relUri[F[_]](rel: String)(implicit F: MonadThrow[F]): F[Uri] =
        F.fromOption(res.links.values.find(_.rel == rel).map(_.uri), relNotFoundErr(rel))

      def withLink(newLink: ResourceLink): HttpResource[R] = res.copy(links = res.links + newLink)

      def withLinks(newLinks: Vector[ResourceLink]): HttpResource[R] = res.copy(links = (res.links ++ newLinks))

      def withSelfLink: HttpResource[R] = withLink(selfLink)

      def linkHeader: Link = res.links.values.toList match {
        case head :: tail => Link(head.linkValueHeader, tail.map(_.linkValueHeader): _*)
        case Nil => Link(selfLink.linkValueHeader)
      }

      def ok[F[_]](implicit EE: EntityEncoder[F, R]): Response[F] =
        Response(status = Status.Ok)
            .withHeaders(linkHeader)
            .withEntity[R](res.body)

      def created[F[_]](implicit EE: EntityEncoder[F, R]): Response[F] =
        Response(status = Status.Created)
            .withHeaders(Location(res.uri), linkHeader)
            .withEntity[R](res.body)
    }

    implicit class UriFunkodeOps(uri: Uri) {

      def link(rel: String): ResourceLink =  ResourceLink(uri, rel)
    }

    implicit class ResourceLinksOps(resourceLinks: ResourceLinks) {

      def +(link: ResourceLink): ResourceLinks = resourceLinks + (link.rel -> link)
      def ++(newLinks: Vector[ResourceLink]): ResourceLinks = resourceLinks ++ newLinks.map(link => (link.rel -> link))
    }

    implicit class ResourceLinkOps(resourceLink: ResourceLink) {

      def withRel(newRel: String): ResourceLink = resourceLink.copy(rel = newRel)
      def linkValueHeader: LinkValue = LinkValue(resourceLink.uri, resourceLink.rel.some)
    }
  }
}
