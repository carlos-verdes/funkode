/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.rest

import java.math.BigInteger
import java.security._
import java.util.UUID

import cats.effect.{IO, MonadThrow, Sync}
import cats.tagless.{autoFunctorK, autoSemigroupalK, finalAlg}
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import io.estatico.newtype.macros.newtype
import org.web3j.crypto.{ECDSASignature, Hash, Keys}
import org.web3j.crypto.Keys.toChecksumAddress
import org.web3j.crypto.Sign.{SignatureData, recoverFromSignature}
import org.web3j.utils.Numeric.hexStringToByteArray
import pureconfig.ConfigSource
import tsec.jws.mac.JWTMac
import tsec.jwt.JWTClaims
import tsec.mac.jca.HMACSHA256


object auth {

  // $COVERAGE-OFF$
  @newtype case class Nonce(value: String)
  @newtype case class Subject(value: String)
  case class Claim(subject: Subject)
  @newtype case class Token(value: String)

  @newtype case class Message(value: String)
  @newtype case class Signature(value: String)
  // $COVERAGE-ON$

  @finalAlg
  @autoFunctorK
  @autoSemigroupalK
  trait AuthDsl[F[_]] {

    def createNonce(subject: Subject): F[Nonce]
    def validateSubject(subject: Subject): F[Subject]
    def validateMessage(msg: Message, signature: Signature, subject: Subject): F[Unit]

    def createToken(subject: Subject): F[Token]
    def validateToken(token: Token): F[Claim]

    def createToken(claim: Claim): F[Token] = createToken(claim.subject)
  }

  object evmJwt {

    import error._
    import pureconfig.generic.auto._
    import tsec.common._

    val ETH_ADDRESS_REGEX = "^0x[0-9a-f]{40}$".r
    val PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n"
    val SIGNATURE_ERROR = ForbiddenError(None, "Wrong signature".some, None)
    val WRONG_ETH_ADDRESS_ERROR = BadRequestError(None, Some("Invalid address"), None)
    val MISSING_SUBJECT_ERR = ForbiddenError(None, Some("Invalid token, subject is missing"), None)

    val INDEX_0 = 0
    val NUMBER_27 = 27
    val INDEX_32 = 32

    val INDEX_64 = 64

    case class JwtConfig(signingKey: String)
    def at(conf: ConfigSource = ConfigSource.default): ConfigSource = conf.at("jwt")
    def load(conf: ConfigSource = ConfigSource.default): JwtConfig = at(conf).loadOrThrow[JwtConfig]

    private def canonical(address: Subject): Subject = Subject(toChecksumAddress(address.value))
    private def canonical(address: String): Subject = canonical(Subject(address))

    lazy val jwtConf: JwtConfig = load()
    lazy val signingKey = HMACSHA256.unsafeBuildKey(jwtConf.signingKey.b64Bytes.get)

    class JwtSecurityInterpreter[F[_]](
        implicit F: MonadThrow[F],
        S: Sync[F],
        J: tsec.jws.mac.JWSMacCV[F, tsec.mac.jca.HMACSHA256]) extends AuthDsl[F] {

      override def createNonce(subject: Subject): F[Nonce] = Nonce(UUID.randomUUID().toString).pure

      override def validateSubject(subject: Subject): F[Subject] =
        if (ETH_ADDRESS_REGEX.matches(subject.value.toLowerCase)){
          canonical(subject).pure[F]
        } else {
          F.raiseError(WRONG_ETH_ADDRESS_ERROR)
        }

      override def validateMessage(msg: Message, signature: Signature, subject: Subject): F[Unit] = {

        val prefixedMessage = PERSONAL_MESSAGE_PREFIX + msg.value.length + msg.value
        val messageHash = Hash.sha3(prefixedMessage.getBytes)
        val canonicalAddress = canonical(subject)

        val signatureBytes = hexStringToByteArray(signature.value)
        val aux = signatureBytes(INDEX_64)

        val v: Byte = if (aux < NUMBER_27.toByte) (aux + NUMBER_27.toByte).toByte else aux
        val r = java.util.Arrays.copyOfRange(signatureBytes, INDEX_0, INDEX_32)
        val s = java.util.Arrays.copyOfRange(signatureBytes, INDEX_32, INDEX_64)

        val sd = new SignatureData(v, r, s)
        val ecdaSignature = new ECDSASignature(new BigInteger(1, sd.getR), new BigInteger(1, sd.getS))

        var found = false
        var i = 0
        while(!found && i < 4) {
          val candidate = recoverFromSignature(i, ecdaSignature, messageHash)

          if (candidate != null && canonical("0x" + Keys.getAddress(candidate)) == canonicalAddress) found = true

          i = i + 1
        }

        if (found) ().pure else F.raiseError(SIGNATURE_ERROR)
      }

      override def createToken(subject: Subject): F[Token] = {

        val jwtClaims = JWTClaims(subject = subject.value.some)
        JWTMac.buildToString[F, HMACSHA256](jwtClaims, signingKey).map(Token(_))
      }

      override def validateToken(token: Token): F[Claim] =
        JWTMac
            .verifyAndParse[F, HMACSHA256](token.value, signingKey)
            .flatMap(result => F.fromOption(result.body.subject, MISSING_SUBJECT_ERR))
            .map(sub => Claim(Subject(sub)))
    }

    object ioEvmJwtSecurityInterpreter extends JwtSecurityInterpreter[IO]

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
  }
}
