/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.arango
package codecs

import avokka.velocypack._
import cats.implicits.toShow
import cats.syntax.either._
import cats.syntax.option._
import io.funkode.rest.resource.HttpResource
import org.http4s.{ParseFailure, Uri}
import org.http4s.headers.{Link, LinkValue}


trait Http4sCodecs {

  implicit val uriDeserializer: VPackDecoder[Uri] = VPackDecoder[String].flatMap(uriString =>
    Uri.fromString(uriString) match {
      case Left(ParseFailure(sanitized, details)) =>
        Left(VPackError.IllegalValue(s"Uri format error, sanitized: $sanitized, details: $details"))
      case Right(uri) =>
        (if (uri.scheme.isEmpty && uri.authority.isEmpty && !uri.path.absolute) {
          uri.withPath(uri.path.toAbsolute)
        } else {
          uri
        }).asRight
    })

  //case class Edge(`_from`: Uri, `_to`: Uri, `_rel`: String, attributes: Map[String, String] = Map.empty)
  case class Edge(uri: Uri, rel: String)

  implicit val edgeDeserializer: VPackDecoder[Edge] = VPackDecoder.gen[Edge]

  implicit val linkValueDecoder: VPackDecoder[LinkValue] = VPackDecoder[Edge].flatMap(edge =>
    LinkValue(edge.uri, edge.rel.some).asRight)

  implicit val linkDecoder: VPackDecoder[Link] = VPackDecoder[Vector[LinkValue]].flatMap(linksVector =>
      if(linksVector.isEmpty) {
        Left(VPackError.IllegalValue(s"Link section can't be empty, make sure you add at least self uri"))
      } else {
        Link(linksVector.head, linksVector.tail: _*).asRight
      })

  implicit def httpResourceDecoder[R](implicit D: VPackDecoder[R]): VPackDecoder[HttpResource[R]] =
    (v: VPack) => v match {
      case VObject(obj) =>

        logger.info(s"decoding http resource " + v.show)
        val result = (obj.get("uri"), obj.get("body"), obj.get("links")) match {
          case (Some(uriVpack), Some(bodyVpack), Some(linksVpack)) =>
            for {
              uri <- uriDeserializer.decode(uriVpack)
              body <- D.decode(bodyVpack)
              links <- linkDecoder.decode(linksVpack)
            } yield HttpResource(uri, body, links)
          case _ => Left(VPackError.IllegalValue(s"HttpResource should have uri, body and links"))
        }
        logger.info(s"decoding result: ${result}")
        result
      case _ => Left(VPackError.IllegalValue(s"HttpResource should be object"))
    }
}
