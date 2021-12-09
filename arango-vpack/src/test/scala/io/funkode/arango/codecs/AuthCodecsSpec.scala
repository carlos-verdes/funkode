/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode
package arango
package codecs

import cats.effect.IO
import org.specs2.Specification
import org.specs2.matcher.{IOMatchers, MatchResult, RestMatchers}
import org.specs2.specification.core.SpecStructure


trait AuthTerms {

  import rest.auth._

  val nonce = Nonce("nonce")
  val subject = Subject("subject")
  val token = Token("token")
  val message = Message("message")
  val signature = Signature("signature")

}

class AuthCodecsSpec
    extends Specification
    with AuthTerms
    with RestMatchers[IO]
    with IOMatchers {

  import authCodecs._

  def is: SpecStructure =
    s2"""
        Auth Codecs should: <br/>
        Encode / Decode nonce $encodeDecodeNonce
        Encode / Decode subject $encodeDecodeSubject
        Encode / Decode token $encodeDecodeToken
        Encode / Decode message $encodeDecodeMessage
        Encode / Decode signature $encodeDecodeSignature
        """

  def encodeDecodeNonce: MatchResult[Any] =
    nonceDecoder.decode(nonceEncoder.encode(nonce)) must beRight(nonce)

  def encodeDecodeSubject: MatchResult[Any] =
    subjectDecoder.decode(subjectEncoder.encode(subject)) must beRight(subject)

  def encodeDecodeToken: MatchResult[Any] =
    tokenDecoder.decode(tokenEncoder.encode(token)) must beRight(token)

  def encodeDecodeMessage: MatchResult[Any] =
    messageDecoder.decode(messageEncoder.encode(message)) must beRight(message)

  def encodeDecodeSignature: MatchResult[Any] =
    signatureDecoder.decode(signatureEncoder.encode(signature)) must beRight(signature)
}
