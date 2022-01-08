/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.rest

import java.security.{NoSuchAlgorithmException, SecureRandom, Security}

import cats.effect.IO
import cats.implicits.catsSyntaxFlatMapOps
import io.funkode.rest
import org.specs2.Specification
import org.specs2.matcher.{IOMatchers, MatchResult, RestMatchers}
import org.specs2.specification.core.SpecStructure

trait JwtClaims {

  import auth._

  val subj = Subject("address1")
  val expectedClaim = Claim(subj)
  val tokenWihtoutSubject = Token(
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
        "eyJyYW5kb20iOiJhZGRyZXNzMSJ9." +
        "ynbxqXi2xyM4w_tDgdYYYTbyMw2pmB3JqCNWYNw1RBA")
}

trait MessagesAndSignatures {

  import auth._

  val msg = Message("v0G9u7huK4mJb2K1")

  val validSig = Signature("0x2c6401216c9031b9a6fb8cbfccab4fcec6c951cdf40e2320108d1856eb532250576865fbcd452bcdc4c573" +
      "21b619ed7a9cfd38bd973c3e1e0243ac2777fe9d5b01")

  val signature2 = Signature("0x2c6401216c9031b9a6fb8cbfccab4fcec6c951cdf40e2320108d1856eb532250576865fbcd452bcdc4c57" +
      "321b619ed7a9cfd38bd973c3e1e0243ac2777fe9d5b1b")

  val walletAddress = Subject("0x31b26e43651e9371c88af3d36c14cfd938baf4fd")
  val canonicalAddress = Subject("0x31b26E43651e9371C88aF3D36c14CfD938BaF4Fd")
  val otherAddress = Subject("0xef678007d18427e6022059dbc264f27507cd1ffc")
  val wrongFormatAddress = Subject("ef678007d18427e6022059dbc264f27507cd1ffc")
}

trait AuthTerms {

  import rest.auth._

  val nonce = Nonce("nonce")
  val subject = Subject("subject")
  val token = Token("token")
  val message = Message("message")
  val signature = Signature("signature")
}

class AuthSpec
    extends Specification
    with JwtClaims
    with MessagesAndSignatures
    with AuthTerms
    with RestMatchers[IO]
    with IOMatchers {

  def is: SpecStructure =

    s2"""
        Auth Algebra should: <br/>

        Pass valid address and format $passValidAddress
        Reject wrong address          $rejectWrongAddress
        Pass valid signed message     $passValidSignature
        Reject wrong signataure       $rejectWrongSignature
        Create a token from claim and get back    $createValidToken
        Raise error if token doesn't have subject $noSubjectError
        Create different nonce        $createTwoNonce

        <br/>Codecs should: <br/>
        Encode / Decode nonce $encodeDecodeNonce
        Encode / Decode subject $encodeDecodeSubject
        Encode / Decode token $encodeDecodeToken
        Encode / Decode message $encodeDecodeMessage
        Encode / Decode signature $encodeDecodeSignature
        """

  import error._
  import auth._

  implicit val dsl = evmJwt.ioEvmJwtSecurityInterpreter
  import dsl._

  // Windows hack
  private def tsecWindowsFix(): Unit =
    try {
      SecureRandom.getInstance("NativePRNGNonBlocking")
      ()
    } catch {
      case _: NoSuchAlgorithmException =>
        val secureRandom = new SecureRandom()
        val defaultSecureRandomProvider = secureRandom.getProvider.get(s"SecureRandom.${secureRandom.getAlgorithm}")
        secureRandom.getProvider.put("SecureRandom.NativePRNGNonBlocking", defaultSecureRandomProvider)
        Security.addProvider(secureRandom.getProvider)
        ()
    }

  tsecWindowsFix()

  def passValidAddress: MatchResult[Any] = dsl.validateSubject(walletAddress) must returnValue(canonicalAddress)

  def rejectWrongAddress: MatchResult[Any] =
    dsl.validateSubject(wrongFormatAddress) must returnError[Subject, BadRequestError]

  def passValidSignature: MatchResult[Any] =
    dsl.validateMessage(msg, validSig, walletAddress) must returnValue(())

  def rejectWrongSignature: MatchResult[Any] =
    dsl.validateMessage(msg, signature2, otherAddress) must returnError[Unit, ForbiddenError]

  def createValidToken: MatchResult[IO[Claim]] = (createToken(subj) >>= validateToken) must returnValue(expectedClaim)
  def noSubjectError: MatchResult[Any] = validateToken(tokenWihtoutSubject)  must returnError[Claim, ForbiddenError]

  def createTwoNonce: MatchResult[Any] = {
    val twoNonces =
      for {
        nonce1 <- dsl.createNonce(subj)
        nonce2 <- dsl.createNonce(subj)
      } yield (nonce1, nonce2)

    twoNonces must returnValue {
      (t: (Nonce, Nonce)) => t._1 mustNotEqual(t._2)
    }
  }

  def encodeDecodeNonce: MatchResult[Any] =
    nonceJsonDecoder.decodeJson(nonceJsonEncoder.apply(nonce)) must beRight(nonce)

  def encodeDecodeSubject: MatchResult[Any] =
    subjectJsonDecoder.decodeJson(subjectJsonEncoder.apply(subject)) must beRight(subject)

  def encodeDecodeToken: MatchResult[Any] =
    tokenJsonDecoder.decodeJson(tokenJsonEncoder.apply(token)) must beRight(token)

  def encodeDecodeMessage: MatchResult[Any] =
    messageJsonDecoder.decodeJson(messageJsonEncoder.apply(message)) must beRight(message)

  def encodeDecodeSignature: MatchResult[Any] =
    signatureJsonDecoder.decodeJson(signatureJsonEncoder.apply(signature)) must beRight(signature)
}
