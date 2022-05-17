/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.arango
package codecs

import avokka.velocypack._
import cats.syntax.either._
import cats.syntax.option._
import org.http4s.Uri
import org.http4s.headers.LinkValue
import org.http4s.implicits.http4sLiteralsSyntax

trait Http4sCodecs {

  implicit val uriDeserializer: VPackDecoder[Uri] = VPackDecoder[String].flatMap(uriString =>
    uriString.split("/") match {
      case Array(col, id) => (uri"/" / col / id).asRight
      case _ => Left(VPackError.IllegalValue(s"Uri format not expected, $uriString doesn't match col/id"))
    }
  )

  case class Edge(`_from`: Uri, `_to`: Uri, `_rel`: String, attributes: Map[String, String] = Map.empty)

  implicit val edgeDeserializer: VPackDecoder[Edge] = VPackDecoder.gen[Edge]

  implicit val linkValueDecoder: VPackDecoder[LinkValue] = VPackDecoder[Edge].flatMap(edge =>
    LinkValue(edge.`_to`, edge.`_rel`.some).asRight)
}
