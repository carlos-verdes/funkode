/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode
package arango
package codecs

import avokka.velocypack._
import cats.implicits.toShow
import cats.syntax.either._
import org.http4s.{ParseFailure, Uri}


trait Http4sCodecs {

  import rest.resource._

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

  implicit val linkValueDecoder: VPackDecoder[ResourceLink] = VPackDecoder.gen[ResourceLink]

  implicit val linkDecoder: VPackDecoder[ResourceLinks] =
    VPackDecoder[Vector[ResourceLink]].flatMap(l => ResourceLinks(l).asRight)

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
