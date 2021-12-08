/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode
package arango
package codecs

import avokka.velocypack.{VPackDecoder, VPackEncoder}


trait AuthCodecs {

  import rest.auth._

  implicit val nonceEncoder: VPackEncoder[Nonce] = VPackEncoder[String].contramap(_.value)
  implicit val nonceDecoder: VPackDecoder[Nonce] = VPackDecoder[String].flatMap(s => Right(Nonce(s)))
  implicit val subjectEncoder: VPackEncoder[Subject] = VPackEncoder[String].contramap(_.value)
  implicit val subjectDecoder: VPackDecoder[Subject] = VPackDecoder[String].flatMap(s => Right(Subject(s)))
  implicit val tokenEncoder: VPackEncoder[Token] = VPackEncoder[String].contramap(_.value)
  implicit val tokenDecoder: VPackDecoder[Token] = VPackDecoder[String].flatMap(s => Right(Token(s)))
  implicit val messageEncoder: VPackEncoder[Message] = VPackEncoder[String].contramap(_.value)
  implicit val messageDecoder: VPackDecoder[Message] = VPackDecoder[String].flatMap(s => Right(Message(s)))
  implicit val signatureEncoder: VPackEncoder[Signature] = VPackEncoder[String].contramap(_.value)
  implicit val signatureDecoder: VPackDecoder[Signature] = VPackDecoder[String].flatMap(s => Right(Signature(s)))
}
