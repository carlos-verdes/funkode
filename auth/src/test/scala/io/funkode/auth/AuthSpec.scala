package io.funkode.auth

import zio.*
import zio.test.*

trait JwtClaims:

  val subj = Subject("address1")
  val expectedClaim = Claim(Some(subj))
  val tokenWihtoutSubject = Token(
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
      "eyJyYW5kb20iOiJhZGRyZXNzMSJ9." +
      "ynbxqXi2xyM4w_tDgdYYYTbyMw2pmB3JqCNWYNw1RBA"
  )

trait MessagesAndSignatures:

  val msg = Message("v0G9u7huK4mJb2K1")

  val validSig = Signature(
    "0x2c6401216c9031b9a6fb8cbfccab4fcec6c951cdf40e2320108d1856eb532250576865fbcd452bcdc4c573" +
      "21b619ed7a9cfd38bd973c3e1e0243ac2777fe9d5b01"
  )

  val signature2 = Signature(
    "0x2c6401216c9031b9a6fb8cbfccab4fcec6c951cdf40e2320108d1856eb532250576865fbcd452bcdc4c57" +
      "321b619ed7a9cfd38bd973c3e1e0243ac2777fe9d5b1b"
  )

  val walletAddress = Subject("0x31b26e43651e9371c88af3d36c14cfd938baf4fd")
  val canonicalAddress = Subject("0x31b26E43651e9371C88aF3D36c14CfD938BaF4Fd")
  val otherAddress = Subject("0xef678007d18427e6022059dbc264f27507cd1ffc")
  val wrongFormatAddress = Subject("ef678007d18427e6022059dbc264f27507cd1ffc")

trait AuthTerms:

  val nonce = Nonce("nonce")
  val subject = Subject("subject")
  val token = Token("token")
  val message = Message("message")
  val signature = Signature("signature")

object AuthSpec extends ZIOSpecDefault with JwtClaims with MessagesAndSignatures with AuthTerms:

  import Auth.*
  import AuthError.*
  import Assertion.*

  override def spec: Spec[TestEnvironment, Any] =
    suite("Auth should")(
      test("Pass valid address and format $passValidAddress")(
        assertZIO(Auth.validateSubject(walletAddress))(equalTo(canonicalAddress))
      ),
      test("Reject wrong address")(
        assertZIO(Auth.validateSubject(wrongFormatAddress).exit)(
          fails(equalTo(WrongRequest(wrongFormatAddress, InvalidAddress)))
        )
      ),
      test("Pass valid signed message")(
        assertZIO(Auth.validateMessage(msg, validSig, walletAddress))(isUnit)
      ),
      test("Reject wrong signataure")(
        assertZIO(Auth.validateMessage(msg, signature2, otherAddress).exit)(failsWithA[AuthError])
      ),
      test("Create a token from claim and get back")(
        assertZIO(Auth.createToken(Claim(Some(subj))).flatMap(Auth.validateToken))(equalTo(expectedClaim))
      ),
      test("Raise error if token doesn 't have subject")(
        assertZIO(Auth.validateToken(tokenWihtoutSubject).exit)(failsWithA[AuthError])
      ),
      test("Create different nonce")(
        for
          nonce1 <- Auth.createNonce(subj)
          nonce2 <- Auth.createNonce(subj)
        yield assertTrue(nonce1.unwrap != nonce2.unwrap)
      )
    ).provideSome(JwtConfiguration.default, Auth.evmJwtAuth)
